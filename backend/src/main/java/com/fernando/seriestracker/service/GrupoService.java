package com.fernando.seriestracker.service;

import com.fernando.seriestracker.config.UsuarioActualService;
import com.fernando.seriestracker.dto.AmigoDTO;
import com.fernando.seriestracker.dto.GrupoDTO;
import com.fernando.seriestracker.dto.GrupoDetalleDTO;
import com.fernando.seriestracker.dto.GrupoInvitacionDTO;
import com.fernando.seriestracker.entity.Grupo;
import com.fernando.seriestracker.entity.GrupoInvitacion;
import com.fernando.seriestracker.entity.GrupoInvitacion.EstadoInvitacion;
import com.fernando.seriestracker.entity.GrupoMiembro;
import com.fernando.seriestracker.entity.Perfil;
import com.fernando.seriestracker.repository.GrupoInvitacionRepository;
import com.fernando.seriestracker.repository.GrupoMiembroRepository;
import com.fernando.seriestracker.repository.GrupoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GrupoService {

    private final GrupoRepository grupoRepository;
    private final GrupoMiembroRepository grupoMiembroRepository;
    private final GrupoInvitacionRepository grupoInvitacionRepository;
    private final AmistadService amistadService;
    private final PerfilService perfilService;
    private final UsuarioActualService usuarioActual;

    @Transactional
    public GrupoDTO crear(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El grupo necesita un nombre");
        }
        UUID yo = usuarioActual.obtenerId();
        Grupo grupo = grupoRepository.save(new Grupo(nombre.trim(), yo));
        grupoMiembroRepository.save(new GrupoMiembro(grupo.getId(), yo));
        return new GrupoDTO(grupo.getId(), grupo.getNombre(), grupo.getFechaCreacion(), 1);
    }

    @Transactional(readOnly = true)
    public List<GrupoDTO> misGrupos() {
        UUID yo = usuarioActual.obtenerId();
        return grupoMiembroRepository.findByUsuarioId(yo).stream()
                .map(m -> grupoRepository.findById(m.getGrupoId()).orElseThrow())
                .map(g -> new GrupoDTO(g.getId(), g.getNombre(), g.getFechaCreacion(),
                        grupoMiembroRepository.findByGrupoId(g.getId()).size()))
                .toList();
    }

    @Transactional(readOnly = true)
    public GrupoDetalleDTO detalle(Long grupoId) {
        comprobarMiembro(grupoId);
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new IllegalArgumentException("No existe ese grupo"));

        List<AmigoDTO> miembros = grupoMiembroRepository.findByGrupoId(grupoId).stream()
                .map(m -> new AmigoDTO(m.getUsuarioId(), emailDe(m.getUsuarioId())))
                .toList();

        return new GrupoDetalleDTO(grupo.getId(), grupo.getNombre(), grupo.getFechaCreacion(), miembros);
    }

    @Transactional
    public void invitar(Long grupoId, String email) {
        UUID yo = usuarioActual.obtenerId();
        comprobarMiembro(grupoId);

        Perfil perfilInvitado = perfilService.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No hay ningún usuario registrado con ese email"));
        UUID invitadoId = perfilInvitado.getUsuarioId();

        if (!amistadService.sonAmigos(yo, invitadoId)) {
            throw new IllegalArgumentException("Solo puedes invitar a amigos al grupo");
        }
        if (grupoMiembroRepository.existsByGrupoIdAndUsuarioId(grupoId, invitadoId)) {
            throw new IllegalArgumentException("Esa persona ya es miembro del grupo");
        }
        grupoInvitacionRepository.findByGrupoIdAndParaUsuarioIdAndEstado(grupoId, invitadoId, EstadoInvitacion.PENDIENTE)
                .ifPresent(i -> { throw new IllegalArgumentException("Ya hay una invitación pendiente para esa persona"); });

        grupoInvitacionRepository.save(new GrupoInvitacion(grupoId, yo, invitadoId));
    }

    @Transactional(readOnly = true)
    public List<GrupoInvitacionDTO> invitacionesRecibidas() {
        UUID yo = usuarioActual.obtenerId();
        return grupoInvitacionRepository.findByParaUsuarioIdAndEstado(yo, EstadoInvitacion.PENDIENTE).stream()
                .map(inv -> {
                    Grupo grupo = grupoRepository.findById(inv.getGrupoId()).orElseThrow();
                    return new GrupoInvitacionDTO(inv.getId(), grupo.getId(), grupo.getNombre(),
                            emailDe(inv.getDeUsuarioId()), inv.getFechaInvitacion());
                })
                .toList();
    }

    @Transactional
    public void aceptarInvitacion(Long invitacionId) {
        GrupoInvitacion invitacion = obtenerInvitacionPendiente(invitacionId);
        invitacion.setEstado(EstadoInvitacion.ACEPTADA);
        grupoInvitacionRepository.save(invitacion);
        grupoMiembroRepository.save(new GrupoMiembro(invitacion.getGrupoId(), invitacion.getParaUsuarioId()));
    }

    @Transactional
    public void rechazarInvitacion(Long invitacionId) {
        GrupoInvitacion invitacion = obtenerInvitacionPendiente(invitacionId);
        invitacion.setEstado(EstadoInvitacion.RECHAZADA);
        grupoInvitacionRepository.save(invitacion);
    }

    private GrupoInvitacion obtenerInvitacionPendiente(Long invitacionId) {
        GrupoInvitacion invitacion = grupoInvitacionRepository.findById(invitacionId)
                .orElseThrow(() -> new IllegalArgumentException("No existe esa invitación"));
        if (!invitacion.getParaUsuarioId().equals(usuarioActual.obtenerId())) {
            throw new IllegalArgumentException("Esa invitación no es tuya");
        }
        if (invitacion.getEstado() != EstadoInvitacion.PENDIENTE) {
            throw new IllegalArgumentException("Esa invitación ya fue respondida");
        }
        return invitacion;
    }

    @Transactional
    public void salir(Long grupoId) {
        comprobarMiembro(grupoId);
        grupoMiembroRepository.deleteByGrupoIdAndUsuarioId(grupoId, usuarioActual.obtenerId());
    }

    // Usado por los controllers de contenido de grupo para autorizar el acceso.
    @Transactional(readOnly = true)
    public void comprobarMiembro(Long grupoId) {
        if (!grupoMiembroRepository.existsByGrupoIdAndUsuarioId(grupoId, usuarioActual.obtenerId())) {
            throw new IllegalArgumentException("No eres miembro de ese grupo");
        }
    }

    private String emailDe(UUID usuarioId) {
        return perfilService.buscarPorId(usuarioId).map(Perfil::getEmail).orElse("(desconocido)");
    }
}

package com.fernando.seriestracker.service;

import com.fernando.seriestracker.config.UsuarioActualService;
import com.fernando.seriestracker.dto.AmigoDTO;
import com.fernando.seriestracker.dto.SolicitudAmistadDTO;
import com.fernando.seriestracker.entity.Amistad;
import com.fernando.seriestracker.entity.Amistad.EstadoAmistad;
import com.fernando.seriestracker.entity.Perfil;
import com.fernando.seriestracker.repository.AmistadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AmistadService {

    private final AmistadRepository amistadRepository;
    private final PerfilService perfilService;
    private final UsuarioActualService usuarioActual;

    @Transactional
    public void enviarSolicitud(String email) {
        UUID yo = usuarioActual.obtenerId();

        Perfil perfilDestino = perfilService.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No hay ningún usuario registrado con ese email"));

        if (perfilDestino.getUsuarioId().equals(yo)) {
            throw new IllegalArgumentException("No puedes enviarte una solicitud a ti mismo");
        }

        amistadRepository.findEntreUsuarios(yo, perfilDestino.getUsuarioId()).ifPresent(existente -> {
            if (existente.getEstado() == EstadoAmistad.ACEPTADA) {
                throw new IllegalArgumentException("Ya sois amigos");
            }
            if (existente.getEstado() == EstadoAmistad.PENDIENTE) {
                throw new IllegalArgumentException("Ya hay una solicitud pendiente entre vosotros");
            }
        });

        amistadRepository.save(new Amistad(yo, perfilDestino.getUsuarioId()));
    }

    @Transactional(readOnly = true)
    public List<SolicitudAmistadDTO> listarRecibidas() {
        return amistadRepository.findByParaUsuarioIdAndEstado(usuarioActual.obtenerId(), EstadoAmistad.PENDIENTE)
                .stream()
                .map(a -> new SolicitudAmistadDTO(a.getId(), a.getDeUsuarioId(), emailDe(a.getDeUsuarioId()), a.getFechaSolicitud()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SolicitudAmistadDTO> listarEnviadas() {
        return amistadRepository.findByDeUsuarioIdAndEstado(usuarioActual.obtenerId(), EstadoAmistad.PENDIENTE)
                .stream()
                .map(a -> new SolicitudAmistadDTO(a.getId(), a.getParaUsuarioId(), emailDe(a.getParaUsuarioId()), a.getFechaSolicitud()))
                .toList();
    }

    @Transactional
    public void aceptar(Long solicitudId) {
        Amistad amistad = obtenerSolicitudRecibida(solicitudId);
        amistad.setEstado(EstadoAmistad.ACEPTADA);
        amistad.setFechaRespuesta(LocalDateTime.now());
        amistadRepository.save(amistad);
    }

    @Transactional
    public void rechazar(Long solicitudId) {
        Amistad amistad = obtenerSolicitudRecibida(solicitudId);
        amistad.setEstado(EstadoAmistad.RECHAZADA);
        amistad.setFechaRespuesta(LocalDateTime.now());
        amistadRepository.save(amistad);
    }

    private Amistad obtenerSolicitudRecibida(Long solicitudId) {
        Amistad amistad = amistadRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("No existe esa solicitud"));
        if (!amistad.getParaUsuarioId().equals(usuarioActual.obtenerId())) {
            throw new IllegalArgumentException("Esa solicitud no te pertenece");
        }
        if (amistad.getEstado() != EstadoAmistad.PENDIENTE) {
            throw new IllegalArgumentException("Esa solicitud ya fue respondida");
        }
        return amistad;
    }

    @Transactional(readOnly = true)
    public List<AmigoDTO> listarAmigos() {
        UUID yo = usuarioActual.obtenerId();
        return amistadRepository.findAmistadesAceptadas(yo).stream()
                .map(a -> {
                    UUID otro = a.getDeUsuarioId().equals(yo) ? a.getParaUsuarioId() : a.getDeUsuarioId();
                    return new AmigoDTO(otro, emailDe(otro));
                })
                .toList();
    }

    @Transactional
    public void eliminarAmistad(UUID otroUsuarioId) {
        UUID yo = usuarioActual.obtenerId();
        Amistad amistad = amistadRepository.findEntreUsuarios(yo, otroUsuarioId)
                .filter(a -> a.getEstado() == EstadoAmistad.ACEPTADA)
                .orElseThrow(() -> new IllegalArgumentException("No sois amigos"));
        amistadRepository.delete(amistad);
    }

    // Usado por los controllers de series/peliculas/mangas para autorizar
    // "ver la colección de un amigo": si no sois amigos, 403.
    @Transactional(readOnly = true)
    public boolean sonAmigos(UUID usuario1, UUID usuario2) {
        return amistadRepository.sonAmigos(usuario1, usuario2);
    }

    private String emailDe(UUID usuarioId) {
        return perfilService.buscarPorId(usuarioId).map(Perfil::getEmail).orElse("(desconocido)");
    }
}

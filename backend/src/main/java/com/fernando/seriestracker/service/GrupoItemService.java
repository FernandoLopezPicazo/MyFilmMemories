package com.fernando.seriestracker.service;

import com.fernando.seriestracker.config.UsuarioActualService;
import com.fernando.seriestracker.dto.GrupoItemDTO;
import com.fernando.seriestracker.dto.OpinionDTO;
import com.fernando.seriestracker.entity.GrupoItem;
import com.fernando.seriestracker.entity.GrupoItem.TipoGrupoItem;
import com.fernando.seriestracker.entity.GrupoItemOpinion;
import com.fernando.seriestracker.entity.Perfil;
import com.fernando.seriestracker.repository.GrupoItemOpinionRepository;
import com.fernando.seriestracker.repository.GrupoItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GrupoItemService {

    private final GrupoItemRepository grupoItemRepository;
    private final GrupoItemOpinionRepository opinionRepository;
    private final GrupoService grupoService;
    private final PerfilService perfilService;
    private final UsuarioActualService usuarioActual;

    @Transactional(readOnly = true)
    public List<GrupoItemDTO> listar(Long grupoId, TipoGrupoItem tipo) {
        grupoService.comprobarMiembro(grupoId);
        return grupoItemRepository.findByGrupoIdAndTipo(grupoId, tipo).stream()
                .map(this::aDTO)
                .toList();
    }

    @Transactional
    public GrupoItemDTO crear(Long grupoId, TipoGrupoItem tipo, String titulo, String descripcion,
                               String imagenUrl, List<String> generos) {
        grupoService.comprobarMiembro(grupoId);
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El título no puede estar vacío");
        }

        GrupoItem item = new GrupoItem();
        item.setGrupoId(grupoId);
        item.setTipo(tipo);
        item.setTitulo(titulo);
        item.setDescripcion(descripcion);
        item.setImagenUrl(imagenUrl);
        item.setGeneros(generos != null ? generos : new ArrayList<>());
        item.setCreadoPor(usuarioActual.obtenerId());

        return aDTO(grupoItemRepository.save(item));
    }

    @Transactional
    public GrupoItemDTO editar(Long grupoId, Long itemId, String titulo, String descripcion,
                                String imagenUrl, List<String> generos) {
        GrupoItem item = obtenerDelGrupo(grupoId, itemId);
        item.setTitulo(titulo);
        item.setDescripcion(descripcion);
        if (imagenUrl != null) item.setImagenUrl(imagenUrl);
        item.setGeneros(generos != null ? generos : new ArrayList<>());
        return aDTO(grupoItemRepository.save(item));
    }

    @Transactional
    public void eliminar(Long grupoId, Long itemId) {
        GrupoItem item = obtenerDelGrupo(grupoId, itemId);
        grupoItemRepository.delete(item);
    }

    @Transactional
    public void opinar(Long grupoId, Long itemId, Integer nota, String personajeFavorito,
                        String personajeOdiado, String comentario) {
        obtenerDelGrupo(grupoId, itemId);
        if (nota != null && (nota < 0 || nota > 10)) {
            throw new IllegalArgumentException("La nota debe estar entre 0 y 10");
        }

        UUID yo = usuarioActual.obtenerId();
        GrupoItemOpinion opinion = opinionRepository.findByGrupoItemIdAndUsuarioId(itemId, yo)
                .orElse(new GrupoItemOpinion(itemId, yo));
        opinion.setNota(nota);
        opinion.setPersonajeFavorito(personajeFavorito);
        opinion.setPersonajeOdiado(personajeOdiado);
        opinion.setComentario(comentario);
        opinionRepository.save(opinion);
    }

    private GrupoItem obtenerDelGrupo(Long grupoId, Long itemId) {
        grupoService.comprobarMiembro(grupoId);
        return grupoItemRepository.findByIdAndGrupoId(itemId, grupoId)
                .orElseThrow(() -> new IllegalArgumentException("No existe ese título en el grupo"));
    }

    private GrupoItemDTO aDTO(GrupoItem item) {
        List<OpinionDTO> opiniones = opinionRepository.findByGrupoItemId(item.getId()).stream()
                .map(o -> new OpinionDTO(o.getUsuarioId(), emailDe(o.getUsuarioId()), o.getNota(),
                        o.getPersonajeFavorito(), o.getPersonajeOdiado(), o.getComentario()))
                .toList();
        return new GrupoItemDTO(item.getId(), item.getTipo(), item.getTitulo(), item.getDescripcion(),
                item.getImagenUrl(), item.getGeneros(), opiniones, item.getSagaId(), item.getEstado());
    }

    private String emailDe(UUID usuarioId) {
        return perfilService.buscarPorId(usuarioId).map(Perfil::getEmail).orElse("(desconocido)");
    }
}

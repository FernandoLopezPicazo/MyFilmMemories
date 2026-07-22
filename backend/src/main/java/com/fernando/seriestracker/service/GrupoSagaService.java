package com.fernando.seriestracker.service;

import com.fernando.seriestracker.config.UsuarioActualService;
import com.fernando.seriestracker.entity.GrupoItem;
import com.fernando.seriestracker.entity.GrupoItem.EstadoGrupoItem;
import com.fernando.seriestracker.entity.GrupoItem.TipoGrupoItem;
import com.fernando.seriestracker.entity.GrupoSaga;
import com.fernando.seriestracker.repository.GrupoItemRepository;
import com.fernando.seriestracker.repository.GrupoSagaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GrupoSagaService {

    private final GrupoSagaRepository grupoSagaRepository;
    private final GrupoItemRepository grupoItemRepository;
    private final GrupoService grupoService;
    private final UsuarioActualService usuarioActual;

    @Transactional(readOnly = true)
    public List<GrupoSaga> obtenerTodas(Long grupoId) {
        grupoService.comprobarMiembro(grupoId);
        List<GrupoSaga> sagas = grupoSagaRepository.findByGrupoId(grupoId);
        // Rellenamos el campo @Transient con los items de cada saga
        for (GrupoSaga saga : sagas) {
            saga.setItems(grupoItemRepository.findBySagaId(saga.getId()));
        }
        return sagas;
    }

    @Transactional
    public GrupoSaga crear(Long grupoId, String titulo) {
        grupoService.comprobarMiembro(grupoId);
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El título de la saga no puede estar vacío");
        }
        GrupoSaga saga = new GrupoSaga();
        saga.setGrupoId(grupoId);
        saga.setTitulo(titulo);
        saga.setEstado(GrupoSaga.EstadoGrupoSaga.EN_PROCESO);
        return grupoSagaRepository.save(saga);
    }

    @Transactional
    public void eliminar(Long grupoId, Long sagaId) {
        grupoService.comprobarMiembro(grupoId);
        GrupoSaga saga = grupoSagaRepository.findByIdAndGrupoId(sagaId, grupoId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la saga con id: " + sagaId));

        // Desvinculamos todos los items antes de borrar la saga
        List<GrupoItem> items = grupoItemRepository.findBySagaId(sagaId);
        for (GrupoItem item : items) {
            item.setSagaId(null);
            grupoItemRepository.save(item);
        }
        grupoSagaRepository.deleteById(saga.getId());
    }

    @Transactional
    public GrupoItem agregarItem(Long grupoId, Long sagaId, String titulo) {
        grupoService.comprobarMiembro(grupoId);
        GrupoSaga saga = grupoSagaRepository.findByIdAndGrupoId(sagaId, grupoId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la saga con id: " + sagaId));

        GrupoItem item = new GrupoItem();
        item.setGrupoId(grupoId);
        item.setTipo(TipoGrupoItem.PELICULA);
        item.setTitulo(titulo);
        item.setSagaId(saga.getId());
        item.setEstado(EstadoGrupoItem.PENDIENTE);
        item.setCreadoPor(usuarioActual.obtenerId());
        return grupoItemRepository.save(item);
    }

    @Transactional
    public void quitarItem(Long grupoId, Long itemId) {
        GrupoItem item = obtenerDelGrupo(grupoId, itemId);
        Long sagaId = item.getSagaId();
        item.setSagaId(null);
        grupoItemRepository.save(item);
        if (sagaId != null) recalcularEstado(sagaId);
    }

    @Transactional
    public void marcarVista(Long grupoId, Long itemId) {
        GrupoItem item = obtenerDelGrupo(grupoId, itemId);
        item.setEstado(EstadoGrupoItem.VISTA);
        grupoItemRepository.save(item);
        if (item.getSagaId() != null) recalcularEstado(item.getSagaId());
    }

    @Transactional
    public void marcarPendiente(Long grupoId, Long itemId) {
        GrupoItem item = obtenerDelGrupo(grupoId, itemId);
        item.setEstado(EstadoGrupoItem.PENDIENTE);
        grupoItemRepository.save(item);
        if (item.getSagaId() != null) recalcularEstado(item.getSagaId());
    }

    // Recalcula el estado de la saga: FINALIZADA si todos sus items están
    // en VISTA (visto compartido para todo el grupo), EN_PROCESO si hay
    // alguno PENDIENTE o si no tiene items.
    @Transactional
    public void recalcularEstado(Long sagaId) {
        GrupoSaga saga = grupoSagaRepository.findById(sagaId).orElse(null);
        if (saga == null) return;

        List<GrupoItem> items = grupoItemRepository.findBySagaId(sagaId);
        boolean finalizada = !items.isEmpty()
                && items.stream().allMatch(i -> i.getEstado() == EstadoGrupoItem.VISTA);

        saga.setEstado(finalizada ? GrupoSaga.EstadoGrupoSaga.FINALIZADA : GrupoSaga.EstadoGrupoSaga.EN_PROCESO);
        grupoSagaRepository.save(saga);
    }

    private GrupoItem obtenerDelGrupo(Long grupoId, Long itemId) {
        grupoService.comprobarMiembro(grupoId);
        return grupoItemRepository.findByIdAndGrupoId(itemId, grupoId)
                .orElseThrow(() -> new IllegalArgumentException("No existe ese título en el grupo"));
    }
}

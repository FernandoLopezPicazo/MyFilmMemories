package com.fernando.seriestracker.service;

import com.fernando.seriestracker.config.UsuarioActualService;
import com.fernando.seriestracker.entity.Pelicula;
import com.fernando.seriestracker.entity.Saga;
import com.fernando.seriestracker.repository.PeliculaRepository;
import com.fernando.seriestracker.repository.SagaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SagaService {

    private final SagaRepository sagaRepository;
    private final PeliculaRepository peliculaRepository;
    private final UsuarioActualService usuarioActual;

    @Transactional(readOnly = true)
    public List<Saga> obtenerTodas() {
        UUID usuarioId = usuarioActual.obtenerId();
        List<Saga> sagas = sagaRepository.findByUsuarioId(usuarioId);
        // Rellenamos el campo @Transient con las películas de cada saga
        for (Saga saga : sagas) {
            List<Pelicula> peliculas = peliculaRepository.findBySagaIdAndUsuarioId(saga.getId(), usuarioId);
            saga.setPeliculas(peliculas);
        }
        return sagas;
    }

    @Transactional
    public Saga crear(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El título de la saga no puede estar vacío");
        }
        Saga saga = new Saga();
        saga.setUsuarioId(usuarioActual.obtenerId());
        saga.setTitulo(titulo);
        saga.setEstado(Saga.EstadoSaga.EN_PROCESO);
        return sagaRepository.save(saga);
    }

    @Transactional
    public void eliminar(Long sagaId) {
        UUID usuarioId = usuarioActual.obtenerId();
        Saga saga = sagaRepository.findByIdAndUsuarioId(sagaId, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la saga con id: " + sagaId));

        // Desvinculamos todas las películas antes de borrar la saga
        List<Pelicula> peliculas = peliculaRepository.findBySagaIdAndUsuarioId(sagaId, usuarioId);
        for (Pelicula p : peliculas) {
            p.setSagaId(null);
            peliculaRepository.save(p);
        }
        sagaRepository.deleteById(saga.getId());
    }

    @Transactional
    public Pelicula agregarPelicula(Long sagaId, String titulo) {
        UUID usuarioId = usuarioActual.obtenerId();
        Saga saga = sagaRepository.findByIdAndUsuarioId(sagaId, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la saga con id: " + sagaId));
        Pelicula nueva = new Pelicula();
        nueva.setUsuarioId(usuarioId);
        nueva.setTitulo(titulo);
        nueva.setEstado(Pelicula.EstadoPelicula.PENDIENTE);
        nueva.setSagaId(saga.getId());
        Pelicula guardada = peliculaRepository.save(nueva);
        recalcularEstado(sagaId);
        return guardada;
    }

    @Transactional
    public void quitarPelicula(Long peliculaId) {
        Pelicula pelicula = peliculaRepository.findByIdAndUsuarioId(peliculaId, usuarioActual.obtenerId())
                .orElseThrow(() -> new IllegalArgumentException("No existe la película con id: " + peliculaId));
        Long sagaId = pelicula.getSagaId();
        pelicula.setSagaId(null);
        peliculaRepository.save(pelicula);
        if (sagaId != null) recalcularEstado(sagaId);
    }

    // Recalcula el estado de la saga: FINALIZADA si todas sus películas son VISTA,
    // EN_PROCESO si hay alguna PENDIENTE o si no tiene películas.
    // Se llama solo desde operaciones que ya verificaron la propiedad del usuario
    // sobre la saga/película de origen, así que aquí no se repite el filtro.
    @Transactional
    public void recalcularEstado(Long sagaId) {
        Saga saga = sagaRepository.findById(sagaId).orElse(null);
        if (saga == null) return;

        List<Pelicula> peliculas = peliculaRepository.findBySagaId(sagaId);
        boolean finalizada = !peliculas.isEmpty()
                && peliculas.stream().allMatch(p -> p.getEstado() == Pelicula.EstadoPelicula.VISTA);

        saga.setEstado(finalizada ? Saga.EstadoSaga.FINALIZADA : Saga.EstadoSaga.EN_PROCESO);
        sagaRepository.save(saga);
    }
}

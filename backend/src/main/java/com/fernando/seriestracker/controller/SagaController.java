package com.fernando.seriestracker.controller;

import com.fernando.seriestracker.entity.Pelicula;
import com.fernando.seriestracker.entity.Saga;
import com.fernando.seriestracker.service.SagaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sagas")
@RequiredArgsConstructor
public class SagaController {

    private final SagaService sagaService;

    @GetMapping
    public ResponseEntity<List<Saga>> listar() {
        return ResponseEntity.ok(sagaService.obtenerTodas());
    }

    @PostMapping
    public ResponseEntity<Saga> crear(@RequestBody Map<String, String> body) {
        String titulo = body.get("titulo");
        Saga saga = sagaService.crear(titulo);
        return ResponseEntity.status(HttpStatus.CREATED).body(saga);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        sagaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Añadir una película nueva directamente a la saga
    @PostMapping("/{sagaId}/peliculas")
    public ResponseEntity<Pelicula> agregarPelicula(
            @PathVariable Long sagaId,
            @RequestBody Map<String, String> body) {
        String titulo = body.get("titulo");
        Pelicula nueva = sagaService.agregarPelicula(sagaId, titulo);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    // Quitar una película de la saga (pasa a ser película suelta)
    @DeleteMapping("/peliculas/{peliculaId}")
    public ResponseEntity<Void> quitarPelicula(@PathVariable Long peliculaId) {
        sagaService.quitarPelicula(peliculaId);
        return ResponseEntity.noContent().build();
    }

    // Vincular una película YA EXISTENTE (suelta o de otra saga) a esta saga
    @PutMapping("/{sagaId}/peliculas/{peliculaId}")
    public ResponseEntity<Pelicula> vincularExistente(
            @PathVariable Long sagaId,
            @PathVariable Long peliculaId) {
        return ResponseEntity.ok(sagaService.vincularExistente(sagaId, peliculaId));
    }

    // Reordenar manualmente las películas de la saga (arrastrar y soltar)
    @PutMapping("/{sagaId}/orden")
    public ResponseEntity<Void> reordenar(
            @PathVariable Long sagaId,
            @RequestBody Map<String, List<Long>> body) {
        sagaService.reordenar(sagaId, body.getOrDefault("ordenIds", List.of()));
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}

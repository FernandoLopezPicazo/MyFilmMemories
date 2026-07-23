package com.fernando.seriestracker.controller;

import com.fernando.seriestracker.entity.GrupoItem;
import com.fernando.seriestracker.entity.GrupoSaga;
import com.fernando.seriestracker.service.GrupoSagaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grupos/{grupoId}/sagas")
@RequiredArgsConstructor
public class GrupoSagaController {

    private final GrupoSagaService grupoSagaService;

    @GetMapping
    public ResponseEntity<List<GrupoSaga>> listar(@PathVariable Long grupoId) {
        return ResponseEntity.ok(grupoSagaService.obtenerTodas(grupoId));
    }

    @PostMapping
    public ResponseEntity<GrupoSaga> crear(@PathVariable Long grupoId, @RequestBody Map<String, String> body) {
        GrupoSaga saga = grupoSagaService.crear(grupoId, body.get("titulo"));
        return ResponseEntity.status(HttpStatus.CREATED).body(saga);
    }

    @DeleteMapping("/{sagaId}")
    public ResponseEntity<Void> eliminar(@PathVariable Long grupoId, @PathVariable Long sagaId) {
        grupoSagaService.eliminar(grupoId, sagaId);
        return ResponseEntity.noContent().build();
    }

    // Añadir una película nueva directamente a la saga del grupo. Acepta
    // opcionalmente descripcion/imagenUrl/generos (resultado del buscador
    // TMDB) además del alta manual con solo título.
    @PostMapping("/{sagaId}/items")
    public ResponseEntity<GrupoItem> agregarItem(
            @PathVariable Long grupoId,
            @PathVariable Long sagaId,
            @RequestBody Map<String, Object> body) {
        GrupoItem item = grupoSagaService.agregarItem(grupoId, sagaId,
                (String) body.get("titulo"),
                (String) body.get("descripcion"),
                (String) body.get("imagenUrl"),
                generos(body));
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    // Vincular un item YA EXISTENTE (suelto o de otra saga) a esta saga
    @PutMapping("/{sagaId}/items/{itemId}")
    public ResponseEntity<GrupoItem> vincularExistente(
            @PathVariable Long grupoId,
            @PathVariable Long sagaId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(grupoSagaService.vincularExistente(grupoId, sagaId, itemId));
    }

    // Reordenar manualmente los items de la saga (arrastrar y soltar)
    @PutMapping("/{sagaId}/orden")
    public ResponseEntity<Void> reordenar(
            @PathVariable Long grupoId,
            @PathVariable Long sagaId,
            @RequestBody Map<String, List<Long>> body) {
        grupoSagaService.reordenar(grupoId, sagaId, body.getOrDefault("ordenIds", List.of()));
        return ResponseEntity.noContent().build();
    }

    // Quitar un item de la saga (pasa a ser título suelto del grupo)
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> quitarItem(@PathVariable Long grupoId, @PathVariable Long itemId) {
        grupoSagaService.quitarItem(grupoId, itemId);
        return ResponseEntity.noContent().build();
    }

    @SuppressWarnings("unchecked")
    private List<String> generos(Map<String, Object> body) {
        return (List<String>) body.get("generos");
    }

    // Marcar como vista: compartido para todo el grupo
    @PutMapping("/items/{itemId}/vista")
    public ResponseEntity<Void> marcarVista(@PathVariable Long grupoId, @PathVariable Long itemId) {
        grupoSagaService.marcarVista(grupoId, itemId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/items/{itemId}/pendiente")
    public ResponseEntity<Void> marcarPendiente(@PathVariable Long grupoId, @PathVariable Long itemId) {
        grupoSagaService.marcarPendiente(grupoId, itemId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}

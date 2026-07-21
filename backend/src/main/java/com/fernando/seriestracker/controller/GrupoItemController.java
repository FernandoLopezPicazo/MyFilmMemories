package com.fernando.seriestracker.controller;

import com.fernando.seriestracker.dto.GrupoItemDTO;
import com.fernando.seriestracker.entity.GrupoItem.TipoGrupoItem;
import com.fernando.seriestracker.service.GrupoItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grupos/{grupoId}/items")
@RequiredArgsConstructor
public class GrupoItemController {

    private final GrupoItemService grupoItemService;

    @GetMapping
    public ResponseEntity<List<GrupoItemDTO>> listar(@PathVariable Long grupoId, @RequestParam TipoGrupoItem tipo) {
        return ResponseEntity.ok(grupoItemService.listar(grupoId, tipo));
    }

    @PostMapping
    public ResponseEntity<GrupoItemDTO> crear(@PathVariable Long grupoId, @RequestBody Map<String, Object> body) {
        GrupoItemDTO creado = grupoItemService.crear(
                grupoId,
                TipoGrupoItem.valueOf((String) body.get("tipo")),
                (String) body.get("titulo"),
                (String) body.get("descripcion"),
                (String) body.get("imagenUrl"),
                generos(body));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<GrupoItemDTO> editar(@PathVariable Long grupoId, @PathVariable Long itemId,
                                                @RequestBody Map<String, Object> body) {
        GrupoItemDTO actualizado = grupoItemService.editar(
                grupoId, itemId,
                (String) body.get("titulo"),
                (String) body.get("descripcion"),
                (String) body.get("imagenUrl"),
                generos(body));
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> eliminar(@PathVariable Long grupoId, @PathVariable Long itemId) {
        grupoItemService.eliminar(grupoId, itemId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{itemId}/opinion")
    public ResponseEntity<Void> opinar(@PathVariable Long grupoId, @PathVariable Long itemId,
                                        @RequestBody Map<String, Object> body) {
        Integer nota = body.get("nota") != null ? ((Number) body.get("nota")).intValue() : null;
        grupoItemService.opinar(grupoId, itemId, nota,
                (String) body.get("personajeFavorito"),
                (String) body.get("personajeOdiado"),
                (String) body.get("comentario"));
        return ResponseEntity.noContent().build();
    }

    @SuppressWarnings("unchecked")
    private List<String> generos(Map<String, Object> body) {
        return (List<String>) body.get("generos");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}

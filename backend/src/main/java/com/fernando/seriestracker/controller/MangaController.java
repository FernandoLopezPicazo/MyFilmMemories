package com.fernando.seriestracker.controller;

import com.fernando.seriestracker.entity.Manga;
import com.fernando.seriestracker.service.MangaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mangas")
@RequiredArgsConstructor
public class MangaController {

    private final MangaService mangaService;

    @GetMapping
    public ResponseEntity<List<Manga>> listar(
            @RequestParam(required = false) Manga.EstadoManga estado) {
        List<Manga> mangas = (estado != null)
                ? mangaService.obtenerPorEstado(estado)
                : mangaService.obtenerTodos();
        return ResponseEntity.ok(mangas);
    }

    @PostMapping
    public ResponseEntity<Manga> crear(@RequestBody Manga manga) {
        Manga creado = mangaService.crear(manga);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}/proceso")
    public ResponseEntity<Void> marcarComoEnProceso(@PathVariable Long id) {
        mangaService.marcarComoEnProceso(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/progreso")
    public ResponseEntity<Void> actualizarProgreso(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Integer capitulo = body.get("capituloActual") != null
                ? ((Number) body.get("capituloActual")).intValue() : null;
        String url = (String) body.get("urlLectura");
        mangaService.actualizarProgreso(id, capitulo, url);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/finalizar")
    public ResponseEntity<Void> finalizar(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Integer nota = body.get("nota") != null
                ? ((Number) body.get("nota")).intValue() : null;
        Manga datos = new Manga();
        datos.setDescripcion((String) body.get("descripcion"));
        datos.setImagenUrl((String) body.get("imagenUrl"));
        datos.setNombrePersona1((String) body.get("nombrePersona1"));
        datos.setPersonajeFavorito((String) body.get("personajeFavorito"));
        datos.setPersonajeOdiado((String) body.get("personajeOdiado"));
        datos.setNombrePersona2((String) body.get("nombrePersona2"));
        datos.setPersonajeFavorito2((String) body.get("personajeFavorito2"));
        datos.setPersonajeOdiado2((String) body.get("personajeOdiado2"));
        if (body.get("nota2") != null) datos.setNota2(((Number) body.get("nota2")).intValue());
        datos.setUrlLectura((String) body.get("urlLectura"));
        @SuppressWarnings("unchecked")
        List<String> generos = (List<String>) body.get("generos");
        datos.setGeneros(generos != null ? generos : new java.util.ArrayList<>());
        mangaService.finalizar(id, datos, nota);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Manga> editar(@PathVariable Long id, @RequestBody Manga manga) {
        Manga actualizado = mangaService.editar(id, manga);
        return ResponseEntity.ok(actualizado);
    }

    @PutMapping("/{id}/pendiente")
    public ResponseEntity<Void> marcarComoPendiente(@PathVariable Long id) {
        mangaService.marcarComoPendiente(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        mangaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}

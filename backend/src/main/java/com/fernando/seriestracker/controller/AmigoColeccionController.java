package com.fernando.seriestracker.controller;

import com.fernando.seriestracker.config.UsuarioActualService;
import com.fernando.seriestracker.entity.Manga;
import com.fernando.seriestracker.entity.Pelicula;
import com.fernando.seriestracker.entity.Serie;
import com.fernando.seriestracker.service.AmistadService;
import com.fernando.seriestracker.service.MangaService;
import com.fernando.seriestracker.service.PeliculaService;
import com.fernando.seriestracker.service.SerieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/*
 * Endpoints de solo lectura para ver la colección de un amigo (lo que no
 * haya marcado como "oculto para amigos"). Antes de devolver nada se
 * comprueba que quien pregunta y el dueño de la colección sean amigos.
 */
@RestController
@RequestMapping("/api/amigos/{usuarioId}")
@RequiredArgsConstructor
public class AmigoColeccionController {

    private final AmistadService amistadService;
    private final SerieService serieService;
    private final PeliculaService peliculaService;
    private final MangaService mangaService;
    private final UsuarioActualService usuarioActual;

    @GetMapping("/series")
    public ResponseEntity<List<Serie>> series(@PathVariable UUID usuarioId) {
        comprobarAmistad(usuarioId);
        return ResponseEntity.ok(serieService.obtenerColeccionVisible(usuarioId));
    }

    @GetMapping("/peliculas")
    public ResponseEntity<List<Pelicula>> peliculas(@PathVariable UUID usuarioId) {
        comprobarAmistad(usuarioId);
        return ResponseEntity.ok(peliculaService.obtenerColeccionVisible(usuarioId));
    }

    @GetMapping("/mangas")
    public ResponseEntity<List<Manga>> mangas(@PathVariable UUID usuarioId) {
        comprobarAmistad(usuarioId);
        return ResponseEntity.ok(mangaService.obtenerColeccionVisible(usuarioId));
    }

    private void comprobarAmistad(UUID otroUsuarioId) {
        if (!amistadService.sonAmigos(usuarioActual.obtenerId(), otroUsuarioId)) {
            throw new SinPermisoException("No sois amigos");
        }
    }

    public static class SinPermisoException extends RuntimeException {
        public SinPermisoException(String mensaje) {
            super(mensaje);
        }
    }

    @ExceptionHandler(SinPermisoException.class)
    public ResponseEntity<Map<String, String>> handleSinPermiso(SinPermisoException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }
}

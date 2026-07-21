package com.fernando.seriestracker.controller;

import com.fernando.seriestracker.dto.ItemSincronizadoDTO;
import com.fernando.seriestracker.entity.Manga;
import com.fernando.seriestracker.entity.Pelicula;
import com.fernando.seriestracker.entity.Serie;
import com.fernando.seriestracker.service.SincronizacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/*
 * El mismo endpoint funciona igual en el backend local del escritorio (perfil
 * "dev", sin login) y en el de la nube (perfil "prod"): ambos usan
 * SincronizacionService, que solo depende de UsuarioActualService — cada uno
 * resuelve "quién soy" a su manera, pero la lógica de fusión es idéntica.
 */
@RestController
@RequestMapping("/api/sincronizacion")
@RequiredArgsConstructor
public class SincronizacionController {

    private final SincronizacionService sincronizacionService;

    @PostMapping("/series")
    public ResponseEntity<List<ItemSincronizadoDTO<Serie>>> series(@RequestBody List<Serie> lote) {
        return ResponseEntity.ok(sincronizacionService.sincronizarSeries(lote));
    }

    @PostMapping("/peliculas")
    public ResponseEntity<List<ItemSincronizadoDTO<Pelicula>>> peliculas(@RequestBody List<Pelicula> lote) {
        return ResponseEntity.ok(sincronizacionService.sincronizarPeliculas(lote));
    }

    @PostMapping("/mangas")
    public ResponseEntity<List<ItemSincronizadoDTO<Manga>>> mangas(@RequestBody List<Manga> lote) {
        return ResponseEntity.ok(sincronizacionService.sincronizarMangas(lote));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}

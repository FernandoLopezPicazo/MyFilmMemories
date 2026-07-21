package com.fernando.seriestracker.controller;

import com.fernando.seriestracker.config.UsuarioActualService;
import com.fernando.seriestracker.service.ImagenStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/*
 * Este controlador recibe un fichero de imagen desde el frontend y devuelve
 * la URL para acceder a él. Dónde se guarda realmente (disco local o
 * Supabase Storage) lo decide ImagenStorageService según el profile activo.
 *
 * MultipartFile: tipo de Spring para recibir ficheros subidos por HTTP.
 */
@RestController
@RequestMapping("/api/imagenes")
@RequiredArgsConstructor
public class ImagenController {

    private final ImagenStorageService imagenStorageService;
    private final UsuarioActualService usuarioActual;

    @PostMapping("/subir")
    public ResponseEntity<Map<String, String>> subirImagen(
            @RequestParam("archivo") MultipartFile archivo) throws IOException {

        String url = imagenStorageService.subir(archivo, usuarioActual.obtenerId());
        return ResponseEntity.ok(Map.of("url", url));
    }
}

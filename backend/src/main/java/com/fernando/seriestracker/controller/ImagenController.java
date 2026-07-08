package com.fernando.seriestracker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

/*
 * Este controlador recibe un fichero de imagen desde el frontend,
 * lo guarda en disco y devuelve la URL para acceder a él.
 *
 * MultipartFile: tipo de Spring para recibir ficheros subidos por HTTP.
 * UUID: genera un nombre único para evitar colisiones si dos series
 *       tienen imágenes con el mismo nombre de fichero.
 */
@RestController
@RequestMapping("/api/imagenes")
@CrossOrigin(origins = "http://localhost:4200")
public class ImagenController {

    // Carpeta donde se guardan las imágenes dentro del proyecto backend
    private static final String UPLOAD_DIR = "uploads/";

    @PostMapping("/subir")
    public ResponseEntity<Map<String, String>> subirImagen(
            @RequestParam("archivo") MultipartFile archivo) throws IOException {

        // Crear la carpeta si no existe
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Nombre único: evita que dos imágenes se sobreescriban
        String nombreFichero = UUID.randomUUID() + "_" + archivo.getOriginalFilename();
        Path destino = uploadPath.resolve(nombreFichero);

        // Guardar el fichero en disco
        Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        // Devolvemos la URL con la que el frontend podrá cargar la imagen
        String url = "/uploads/" + nombreFichero;
        return ResponseEntity.ok(Map.of("url", url));
    }
}

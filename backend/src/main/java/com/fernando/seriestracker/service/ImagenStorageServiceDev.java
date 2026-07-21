package com.fernando.seriestracker.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

/*
 * Guarda la imagen en disco, en la carpeta uploads/ del backend — igual que
 * funcionaba antes de añadir Supabase Storage. Usado por el instalador
 * Electron y por desarrollo local (profile distinto de "prod").
 */
@Service
@Profile("!prod")
public class ImagenStorageServiceDev implements ImagenStorageService {

    private static final String UPLOAD_DIR = "uploads/";

    @Override
    public String subir(MultipartFile archivo, UUID usuarioId) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Nombre único: evita que dos imágenes se sobreescriban
        String nombreFichero = UUID.randomUUID() + "_" + archivo.getOriginalFilename();
        Path destino = uploadPath.resolve(nombreFichero);
        Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/" + nombreFichero;
    }
}

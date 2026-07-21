package com.fernando.seriestracker.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/*
 * Sube una imagen y devuelve la URL con la que el frontend podrá cargarla.
 * Dos implementaciones según el profile activo:
 *   - ImagenStorageServiceDev (profile "dev", instalador Electron/local):
 *     guarda en disco, exactamente como funcionaba antes de esta migración.
 *   - ImagenStorageServiceProd (profile "prod", nube): sube a Supabase
 *     Storage — necesario porque el disco de Render es efímero.
 */
public interface ImagenStorageService {
    String subir(MultipartFile archivo, UUID usuarioId) throws IOException;
}

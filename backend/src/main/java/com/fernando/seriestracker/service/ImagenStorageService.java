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
 *
 * "extension" la calcula y valida ImagenController (jpg/png/webp/gif) a
 * partir del content-type real del fichero — las implementaciones NUNCA
 * deben tocar archivo.getOriginalFilename(): ese nombre lo controla quien
 * sube el fichero y usarlo para construir una ruta o URL permite path
 * traversal (../../) o inyectar segmentos raros en la URL de destino.
 */
public interface ImagenStorageService {
    String subir(MultipartFile archivo, UUID usuarioId, String extension) throws IOException;
}

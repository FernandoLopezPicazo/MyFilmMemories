package com.fernando.seriestracker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/*
 * Sube la imagen a un bucket de Supabase Storage vía su API REST simple
 * (sin SDK S3: es una subida trivial de un único fichero ≤10MB, no necesita
 * multipart/resumable). Cada fichero se prefija con el usuarioId, solo para
 * organización dentro del bucket, no como control de acceso — el bucket es
 * público (MVP), igual de expuesto que /uploads/** lo estaba antes.
 *
 * El SUPABASE_SERVICE_ROLE_KEY nunca llega al frontend: vive únicamente
 * aquí, como variable de entorno del backend.
 */
@Service
@Profile("prod")
public class ImagenStorageServiceProd implements ImagenStorageService {

    @Value("${app.supabase.project-url}")
    private String projectUrl;

    @Value("${app.supabase.service-role-key}")
    private String serviceRoleKey;

    @Value("${app.supabase.storage-bucket}")
    private String bucket;

    @Override
    public String subir(MultipartFile archivo, UUID usuarioId) throws IOException {
        String nombreFichero = usuarioId + "/" + UUID.randomUUID() + "_" + archivo.getOriginalFilename();
        String contentType = archivo.getContentType() != null ? archivo.getContentType() : "application/octet-stream";

        RestClient.create()
                .post()
                .uri(projectUrl + "/storage/v1/object/" + bucket + "/" + nombreFichero)
                .header("Authorization", "Bearer " + serviceRoleKey)
                .header("apikey", serviceRoleKey)
                .contentType(MediaType.parseMediaType(contentType))
                .body(archivo.getBytes())
                .retrieve()
                .toBodilessEntity();

        return projectUrl + "/storage/v1/object/public/" + bucket + "/" + nombreFichero;
    }
}

package com.fernando.seriestracker.controller;

import com.fernando.seriestracker.config.UsuarioActualService;
import com.fernando.seriestracker.service.ImagenStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

/*
 * Este controlador recibe un fichero de imagen desde el frontend y devuelve
 * la URL para acceder a él. Dónde se guarda realmente (disco local o
 * Supabase Storage) lo decide ImagenStorageService según el profile activo.
 *
 * MultipartFile: tipo de Spring para recibir ficheros subidos por HTTP.
 *
 * SEGURIDAD: el nombre de fichero que manda el navegador (getOriginalFilename())
 * NO se usa nunca para construir rutas ni URLs — un nombre como
 * "../../etc/passwd" o con caracteres especiales permitiría escribir fuera
 * de la carpeta de subidas (disco local) o inyectar segmentos raros en la
 * URL de Supabase Storage (nube). Aquí se valida el tipo de contenido
 * contra una lista blanca de imágenes (cabecera Y primeros bytes reales del
 * fichero, para que no baste con mentir en el Content-Type) y se delega a
 * ImagenStorageService, que genera el nombre final íntegramente en el
 * servidor (UUID + extensión ya validada).
 */
@RestController
@RequestMapping("/api/imagenes")
@RequiredArgsConstructor
public class ImagenController {

    private static final long TAMANO_MAXIMO_BYTES = 10L * 1024 * 1024; // 10MB, igual que el límite global de multipart

    // Extensión de destino <- content-type declarado, validado luego contra
    // los "magic bytes" reales del fichero para que no baste con falsear la
    // cabecera Content-Type.
    private static final Map<String, String> TIPOS_PERMITIDOS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/gif", "gif"
    );

    private final ImagenStorageService imagenStorageService;
    private final UsuarioActualService usuarioActual;

    @PostMapping("/subir")
    public ResponseEntity<Map<String, String>> subirImagen(
            @RequestParam("archivo") MultipartFile archivo) throws IOException {

        if (archivo.isEmpty()) {
            return error("El archivo está vacío");
        }
        if (archivo.getSize() > TAMANO_MAXIMO_BYTES) {
            return error("El archivo supera el tamaño máximo permitido (10MB)");
        }

        String contentType = archivo.getContentType() != null
                ? archivo.getContentType().toLowerCase(Locale.ROOT)
                : "";
        String extension = TIPOS_PERMITIDOS.get(contentType);
        if (extension == null) {
            return error("Tipo de archivo no permitido. Solo se aceptan imágenes JPG, PNG, WEBP o GIF");
        }
        if (!empiezaConMagicBytes(archivo, contentType)) {
            return error("El contenido del archivo no coincide con una imagen válida");
        }

        String url = imagenStorageService.subir(archivo, usuarioActual.obtenerId(), extension);
        return ResponseEntity.ok(Map.of("url", url));
    }

    // Comprueba los primeros bytes del fichero (firma real del formato) para
    // que el Content-Type declarado por el cliente no sea la única defensa
    // — cualquier cliente HTTP puede mandar la cabecera que quiera.
    private boolean empiezaConMagicBytes(MultipartFile archivo, String contentType) throws IOException {
        byte[] cabecera = new byte[12];
        try (var in = archivo.getInputStream()) {
            int leidos = in.readNBytes(cabecera, 0, cabecera.length);
            if (leidos < 4) return false;
        }
        return switch (contentType) {
            case "image/jpeg" -> (cabecera[0] & 0xFF) == 0xFF && (cabecera[1] & 0xFF) == 0xD8;
            case "image/png" -> (cabecera[0] & 0xFF) == 0x89 && cabecera[1] == 'P' && cabecera[2] == 'N' && cabecera[3] == 'G';
            case "image/gif" -> cabecera[0] == 'G' && cabecera[1] == 'I' && cabecera[2] == 'F';
            case "image/webp" -> cabecera[0] == 'R' && cabecera[1] == 'I' && cabecera[2] == 'F' && cabecera[3] == 'F'
                    && cabecera.length >= 12 && cabecera[8] == 'W' && cabecera[9] == 'E' && cabecera[10] == 'B' && cabecera[11] == 'P';
            default -> false;
        };
    }

    private ResponseEntity<Map<String, String>> error(String mensaje) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", mensaje));
    }
}

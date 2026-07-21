package com.fernando.seriestracker.controller;

import com.fernando.seriestracker.dto.AmigoDTO;
import com.fernando.seriestracker.dto.SolicitudAmistadDTO;
import com.fernando.seriestracker.service.AmistadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/amigos")
@RequiredArgsConstructor
public class AmistadController {

    private final AmistadService amistadService;

    @PostMapping("/solicitudes")
    public ResponseEntity<Void> enviarSolicitud(@RequestBody Map<String, String> body) {
        amistadService.enviarSolicitud(body.get("email"));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/solicitudes/recibidas")
    public ResponseEntity<List<SolicitudAmistadDTO>> listarRecibidas() {
        return ResponseEntity.ok(amistadService.listarRecibidas());
    }

    @GetMapping("/solicitudes/enviadas")
    public ResponseEntity<List<SolicitudAmistadDTO>> listarEnviadas() {
        return ResponseEntity.ok(amistadService.listarEnviadas());
    }

    @PutMapping("/solicitudes/{id}/aceptar")
    public ResponseEntity<Void> aceptar(@PathVariable Long id) {
        amistadService.aceptar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/solicitudes/{id}/rechazar")
    public ResponseEntity<Void> rechazar(@PathVariable Long id) {
        amistadService.rechazar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<AmigoDTO>> listarAmigos() {
        return ResponseEntity.ok(amistadService.listarAmigos());
    }

    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<Void> eliminarAmistad(@PathVariable UUID usuarioId) {
        amistadService.eliminarAmistad(usuarioId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}

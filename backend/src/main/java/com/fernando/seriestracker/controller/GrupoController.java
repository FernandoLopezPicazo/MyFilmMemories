package com.fernando.seriestracker.controller;

import com.fernando.seriestracker.dto.GrupoDTO;
import com.fernando.seriestracker.dto.GrupoDetalleDTO;
import com.fernando.seriestracker.dto.GrupoInvitacionDTO;
import com.fernando.seriestracker.service.GrupoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grupos")
@RequiredArgsConstructor
public class GrupoController {

    private final GrupoService grupoService;

    @PostMapping
    public ResponseEntity<GrupoDTO> crear(@RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(grupoService.crear(body.get("nombre")));
    }

    @GetMapping
    public ResponseEntity<List<GrupoDTO>> misGrupos() {
        return ResponseEntity.ok(grupoService.misGrupos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GrupoDetalleDTO> detalle(@PathVariable Long id) {
        return ResponseEntity.ok(grupoService.detalle(id));
    }

    @PostMapping("/{id}/invitaciones")
    public ResponseEntity<Void> invitar(@PathVariable Long id, @RequestBody Map<String, String> body) {
        grupoService.invitar(id, body.get("email"));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/invitaciones/recibidas")
    public ResponseEntity<List<GrupoInvitacionDTO>> invitacionesRecibidas() {
        return ResponseEntity.ok(grupoService.invitacionesRecibidas());
    }

    @PutMapping("/invitaciones/{id}/aceptar")
    public ResponseEntity<Void> aceptarInvitacion(@PathVariable Long id) {
        grupoService.aceptarInvitacion(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/invitaciones/{id}/rechazar")
    public ResponseEntity<Void> rechazarInvitacion(@PathVariable Long id) {
        grupoService.rechazarInvitacion(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/salir")
    public ResponseEntity<Void> salir(@PathVariable Long id) {
        grupoService.salir(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}

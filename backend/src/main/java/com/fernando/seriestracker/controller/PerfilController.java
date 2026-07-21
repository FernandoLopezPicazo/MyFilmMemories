package com.fernando.seriestracker.controller;

import com.fernando.seriestracker.service.PerfilService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * El frontend llama a POST /api/perfiles/sincronizar una vez justo tras
 * iniciar sesión (ver auth.service.ts) para que este usuario_id+email quede
 * registrado y se pueda encontrar por email desde el sistema de amigos.
 */
@RestController
@RequestMapping("/api/perfiles")
@RequiredArgsConstructor
public class PerfilController {

    private final PerfilService perfilService;

    @PostMapping("/sincronizar")
    public ResponseEntity<Void> sincronizar() {
        perfilService.sincronizar();
        return ResponseEntity.noContent().build();
    }
}

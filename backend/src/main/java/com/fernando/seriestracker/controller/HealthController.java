package com.fernando.seriestracker.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/*
 * Fuera de /api/**, así que SecurityConfig lo deja pasar sin JWT (ver
 * ".anyRequest().permitAll()"). Lo usa el frontend para saber si el backend
 * de Render ya ha terminado de "despertar" tras estar dormido por inactividad.
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}

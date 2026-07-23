package com.fernando.seriestracker.controller;

import com.fernando.seriestracker.dto.ItemProgramadoDTO;
import com.fernando.seriestracker.service.HorarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/horario")
@RequiredArgsConstructor
public class HorarioController {

    private final HorarioService horarioService;

    @GetMapping
    public ResponseEntity<List<ItemProgramadoDTO>> obtenerHorario() {
        return ResponseEntity.ok(horarioService.obtenerHorario());
    }
}

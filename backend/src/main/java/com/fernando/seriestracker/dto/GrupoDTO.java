package com.fernando.seriestracker.dto;

import java.time.LocalDateTime;

public record GrupoDTO(Long id, String nombre, LocalDateTime fechaCreacion, int numeroMiembros) {
}

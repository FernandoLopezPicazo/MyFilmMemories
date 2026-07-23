package com.fernando.seriestracker.dto;

public record ItemProgramadoDTO(
        Long id,
        String tipo, // SERIE | PELICULA | MANGA
        String titulo,
        String imagenUrl,
        String diaSemana,
        String frecuencia,
        Integer semanaDelMes
) {
}

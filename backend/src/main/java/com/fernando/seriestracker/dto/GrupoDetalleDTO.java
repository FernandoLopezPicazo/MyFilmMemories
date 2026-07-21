package com.fernando.seriestracker.dto;

import java.time.LocalDateTime;
import java.util.List;

public record GrupoDetalleDTO(Long id, String nombre, LocalDateTime fechaCreacion, List<AmigoDTO> miembros) {
}

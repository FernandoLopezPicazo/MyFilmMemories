package com.fernando.seriestracker.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SolicitudAmistadDTO(
        Long id,
        UUID otroUsuarioId,
        String otroEmail,
        LocalDateTime fechaSolicitud
) {
}

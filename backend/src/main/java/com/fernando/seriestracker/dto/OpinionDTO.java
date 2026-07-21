package com.fernando.seriestracker.dto;

import java.util.UUID;

public record OpinionDTO(
        UUID usuarioId,
        String email,
        Integer nota,
        String personajeFavorito,
        String personajeOdiado,
        String comentario
) {
}

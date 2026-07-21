package com.fernando.seriestracker.dto;

import java.time.LocalDateTime;

public record GrupoInvitacionDTO(
        Long id,
        Long grupoId,
        String nombreGrupo,
        String deEmail,
        LocalDateTime fechaInvitacion
) {
}

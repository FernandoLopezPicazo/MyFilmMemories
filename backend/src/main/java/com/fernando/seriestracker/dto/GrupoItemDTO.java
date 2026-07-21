package com.fernando.seriestracker.dto;

import com.fernando.seriestracker.entity.GrupoItem.TipoGrupoItem;

import java.util.List;

public record GrupoItemDTO(
        Long id,
        TipoGrupoItem tipo,
        String titulo,
        String descripcion,
        String imagenUrl,
        List<String> generos,
        List<OpinionDTO> opiniones
) {
}

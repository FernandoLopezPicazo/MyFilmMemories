package com.fernando.seriestracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "grupo_miembros")
@Data
@NoArgsConstructor
public class GrupoMiembro {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "grupo_miembro_seq")
    @SequenceGenerator(name = "grupo_miembro_seq", sequenceName = "GRUPO_MIEMBRO_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "grupo_id", nullable = false)
    private Long grupoId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "fecha_union", nullable = false)
    private LocalDateTime fechaUnion = LocalDateTime.now();

    public GrupoMiembro(Long grupoId, UUID usuarioId) {
        this.grupoId = grupoId;
        this.usuarioId = usuarioId;
    }
}

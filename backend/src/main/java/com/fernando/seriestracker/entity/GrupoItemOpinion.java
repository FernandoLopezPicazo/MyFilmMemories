package com.fernando.seriestracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/*
 * La opinión de UN miembro sobre UN GrupoItem. Cada miembro tiene como mucho
 * una fila por item (se hace upsert desde el service), así que cualquier
 * número de miembros puede opinar sobre el mismo título — generalizando el
 * modelo de "2 personas" de las listas personales a N miembros.
 */
@Entity
@Table(name = "grupo_item_opiniones")
@Data
@NoArgsConstructor
public class GrupoItemOpinion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "grupo_item_opinion_seq")
    @SequenceGenerator(name = "grupo_item_opinion_seq", sequenceName = "GRUPO_ITEM_OPINION_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "grupo_item_id", nullable = false)
    private Long grupoItemId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(nullable = true)
    private Integer nota;

    @Column(name = "personaje_favorito", nullable = true)
    private String personajeFavorito;

    @Column(name = "personaje_odiado", nullable = true)
    private String personajeOdiado;

    @Column(nullable = true, length = 2000)
    private String comentario;

    public GrupoItemOpinion(Long grupoItemId, UUID usuarioId) {
        this.grupoItemId = grupoItemId;
        this.usuarioId = usuarioId;
    }
}

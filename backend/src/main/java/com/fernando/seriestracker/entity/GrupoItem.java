package com.fernando.seriestracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
 * Un título dentro de la lista compartida de un grupo (serie, película o
 * manga — el campo "tipo" distingue cuál). A diferencia de Serie/Pelicula/
 * Manga personales, aquí no hay tres entidades separadas: los campos
 * (título, descripción, imagen, géneros) son idénticos para los tres tipos
 * y no hay seguimiento de progreso por grupo, así que una sola tabla evita
 * triplicar código sin ninguna diferencia real que justificarlo.
 * Las opiniones de cada miembro (nota, personajes...) viven en GrupoItemOpinion.
 */
@Entity
@Table(name = "grupo_items")
@Data
@NoArgsConstructor
public class GrupoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "grupo_item_seq")
    @SequenceGenerator(name = "grupo_item_seq", sequenceName = "GRUPO_ITEM_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "grupo_id", nullable = false)
    private Long grupoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoGrupoItem tipo;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = true, length = 5000)
    private String descripcion;

    @Column(name = "imagen_url", nullable = true)
    private String imagenUrl;

    @Column(name = "creado_por", nullable = false)
    private UUID creadoPor;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    // Solo relevante cuando tipo == PELICULA y el item pertenece a una
    // GrupoSaga; null para items sueltos.
    @Column(name = "saga_id", nullable = true)
    private Long sagaId;

    // Posición manual dentro de su saga (null = sin ordenar / no pertenece
    // a ninguna saga). Se reasigna al arrastrar para reordenar.
    @Column(name = "orden", nullable = true)
    private Integer orden;

    // A diferencia de las listas personales, este estado es COMPARTIDO por
    // todo el grupo (no por miembro): si alguien marca la película como
    // vista, se marca vista para todos. Solo se usa/expone en el contexto
    // de sagas de grupo; los items sueltos se quedan en PENDIENTE sin uso.
    // nullable=true a nivel JPA (aunque el default en código siempre lo
    // rellena) porque H2 en dev usa ddl-auto=update: añadir una columna
    // NOT NULL sin default falla si la tabla ya tiene filas. En Postgres
    // la migración V7 sí la crea NOT NULL DEFAULT 'PENDIENTE'.
    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 20)
    private EstadoGrupoItem estado = EstadoGrupoItem.PENDIENTE;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "grupo_item_generos", joinColumns = @JoinColumn(name = "grupo_item_id"))
    @Column(name = "genero")
    private List<String> generos = new ArrayList<>();

    public enum TipoGrupoItem {
        SERIE, PELICULA, MANGA
    }

    public enum EstadoGrupoItem {
        PENDIENTE, VISTA
    }
}

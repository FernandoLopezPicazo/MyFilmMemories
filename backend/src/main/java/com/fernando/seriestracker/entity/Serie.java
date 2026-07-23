package com.fernando.seriestracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "series")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Serie {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "serie_seq")
    @SequenceGenerator(name = "serie_seq", sequenceName = "SERIE_SEQ", allocationSize = 1)
    private Long id;

    // Propietario del registro (id del usuario en Supabase Auth). Cada usuario
    // solo ve/edita sus propias series — el filtrado ocurre en el Service.
    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = true, length = 5000)
    private String descripcion;

    @Column(name = "imagen_url", nullable = true)
    private String imagenUrl;

    // Persona 1
    @Column(name = "nombre_persona1", nullable = true)
    private String nombrePersona1;

    @Column(name = "personaje_favorito", nullable = true)
    private String personajeFavorito;

    @Column(name = "personaje_odiado", nullable = true)
    private String personajeOdiado;

    @Column(name = "nota", nullable = true)
    private Integer nota;

    // Persona 2
    @Column(name = "nombre_persona2", nullable = true)
    private String nombrePersona2;

    @Column(name = "personaje_favorito2", nullable = true)
    private String personajeFavorito2;

    @Column(name = "personaje_odiado2", nullable = true)
    private String personajeOdiado2;

    @Column(name = "nota2", nullable = true)
    private Integer nota2;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSerie estado = EstadoSerie.PENDIENTE;

    @Column(name = "fecha_vista", nullable = true)
    private LocalDate fechaVista;

    @Column(name = "temporada_actual", nullable = true)
    private Integer temporadaActual;

    @Column(name = "episodio_actual", nullable = true)
    private Integer episodioActual;

    // Si es true, este título no aparece cuando un amigo consulta tu colección.
    @Column(name = "oculto_para_amigos", nullable = false)
    private boolean ocultoParaAmigos = false;

    // Identificador estable usado por la sincronización escritorio↔nube para
    // reconocer "el mismo título" en dos bases de datos con contadores de id
    // independientes. Nulo en filas creadas antes de esta función.
    @Column(name = "sync_id")
    private java.util.UUID syncId;

    @Column(name = "actualizado_en")
    private java.time.LocalDateTime actualizadoEn;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "serie_generos", joinColumns = @JoinColumn(name = "serie_id"))
    @Column(name = "genero")
    private List<String> generos = new ArrayList<>();

    // ── Horario (Fase 6) ───────────────────────────────
    // nullable=true a nivel JPA (aunque el default en código siempre lo
    // rellena) porque H2 en dev usa ddl-auto=update: añadir una columna
    // NOT NULL sin default falla si la tabla ya tiene filas. En Postgres
    // la migración V9 sí la crea con DEFAULT false.
    @Column(name = "en_emision", nullable = true)
    private boolean enEmision = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 20)
    private Frecuencia frecuencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = true, length = 20)
    private DiaSemana diaSemana;

    // 1-5, solo relevante cuando frecuencia == MENSUAL
    @Column(name = "semana_del_mes", nullable = true)
    private Integer semanaDelMes;

    public enum EstadoSerie {
        PENDIENTE, EN_PROCESO, VISTA
    }

    public enum Frecuencia {
        SEMANAL, MENSUAL
    }

    public enum DiaSemana {
        LUNES, MARTES, MIERCOLES, JUEVES, VIERNES, SABADO, DOMINGO
    }
}

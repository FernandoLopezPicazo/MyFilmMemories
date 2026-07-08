package com.fernando.seriestracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "serie_generos", joinColumns = @JoinColumn(name = "serie_id"))
    @Column(name = "genero")
    private List<String> generos = new ArrayList<>();

    public enum EstadoSerie {
        PENDIENTE, EN_PROCESO, VISTA
    }
}

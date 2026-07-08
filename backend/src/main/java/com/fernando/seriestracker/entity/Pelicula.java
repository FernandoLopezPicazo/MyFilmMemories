package com.fernando.seriestracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "peliculas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pelicula {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pelicula_seq")
    @SequenceGenerator(name = "pelicula_seq", sequenceName = "PELICULA_SEQ", allocationSize = 1)
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
    private EstadoPelicula estado = EstadoPelicula.PENDIENTE;

    @Column(name = "fecha_vista", nullable = true)
    private LocalDate fechaVista;

    @Column(name = "duracionMinutos", nullable = true)
    private Integer duracionMinutos;

    // Referencia opcional a una saga (null = película suelta)
    @Column(name = "saga_id", nullable = true)
    private Long sagaId;

    

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pelicula_generos", joinColumns = @JoinColumn(name = "pelicula_id"))
    @Column(name = "genero")
    private List<String> generos = new ArrayList<>();

    public enum EstadoPelicula {
        PENDIENTE, VISTA
    }
}

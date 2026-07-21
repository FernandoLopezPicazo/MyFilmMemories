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
@Table(name = "mangas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Manga {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "manga_seq")
    @SequenceGenerator(name = "manga_seq", sequenceName = "MANGA_SEQ", allocationSize = 1)
    private Long id;

    // Propietario del registro (id del usuario en Supabase Auth). Cada usuario
    // solo ve/edita sus propios mangas — el filtrado ocurre en el Service.
    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = true, length = 5000)
    private String descripcion;

    @Column(name = "imagen_url", nullable = true)
    private String imagenUrl;

    // Progreso (EN_PROCESO)
    @Column(name = "capitulo_actual", nullable = true)
    private Integer capituloActual;

    @Column(name = "url_lectura", nullable = true, length = 500)
    private String urlLectura;

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
    private EstadoManga estado = EstadoManga.PENDIENTE;

    @Column(name = "fecha_finalizado", nullable = true)
    private LocalDate fechaFinalizado;

    // Si es true, este título no aparece cuando un amigo consulta tu colección.
    @Column(name = "oculto_para_amigos", nullable = false)
    private boolean ocultoParaAmigos = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "manga_generos", joinColumns = @JoinColumn(name = "manga_id"))
    @Column(name = "genero")
    private List<String> generos = new ArrayList<>();

    public enum EstadoManga {
        PENDIENTE, EN_PROCESO, FINALIZADO
    }
}

package com.fernando.seriestracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sagas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Saga {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "saga_seq")
    @SequenceGenerator(name = "saga_seq", sequenceName = "SAGA_SEQ", allocationSize = 1)
    private Long id;

    // Propietario del registro (id del usuario en Supabase Auth). Cada usuario
    // solo ve/edita sus propias sagas — el filtrado ocurre en el Service.
    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(nullable = false)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSaga estado = EstadoSaga.EN_PROCESO;

    // Campo transient: no persiste en BD, se rellena desde el servicio al devolver la saga
    @Transient
    private List<Pelicula> peliculas = new ArrayList<>();

    public enum EstadoSaga { EN_PROCESO, FINALIZADA }
}

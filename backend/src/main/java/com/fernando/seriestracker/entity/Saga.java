package com.fernando.seriestracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

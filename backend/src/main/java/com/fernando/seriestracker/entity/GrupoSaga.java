package com.fernando.seriestracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "grupo_sagas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrupoSaga {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "grupo_saga_seq")
    @SequenceGenerator(name = "grupo_saga_seq", sequenceName = "GRUPO_SAGA_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "grupo_id", nullable = false)
    private Long grupoId;

    @Column(nullable = false)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoGrupoSaga estado = EstadoGrupoSaga.EN_PROCESO;

    // Campo transient: no persiste en BD, se rellena desde el servicio al devolver la saga
    @Transient
    private List<GrupoItem> items = new ArrayList<>();

    public enum EstadoGrupoSaga { EN_PROCESO, FINALIZADA }
}

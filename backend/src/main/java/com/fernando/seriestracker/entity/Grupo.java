package com.fernando.seriestracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "grupos")
@Data
@NoArgsConstructor
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "grupo_seq")
    @SequenceGenerator(name = "grupo_seq", sequenceName = "GRUPO_SEQ", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "creado_por", nullable = false)
    private UUID creadoPor;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public Grupo(String nombre, UUID creadoPor) {
        this.nombre = nombre;
        this.creadoPor = creadoPor;
    }
}

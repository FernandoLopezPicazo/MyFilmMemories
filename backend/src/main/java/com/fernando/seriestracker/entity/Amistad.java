package com.fernando.seriestracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/*
 * Una fila = una solicitud de amistad, en un único sentido (de -> a).
 * Cuando "a" la acepta, la misma fila pasa a ACEPTADA y ya cuenta como
 * amistad en ambos sentidos (no se duplica una segunda fila inversa).
 */
@Entity
@Table(name = "amistades")
@Data
@NoArgsConstructor
public class Amistad {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "amistad_seq")
    @SequenceGenerator(name = "amistad_seq", sequenceName = "AMISTAD_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "de_usuario_id", nullable = false)
    private UUID deUsuarioId;

    // Nombrado "paraUsuarioId" (no "aUsuarioId") porque Spring Data JPA no
    // consigue interpretar bien un nombre de campo que empieza con una sola
    // letra minúscula seguida de mayúscula en las consultas derivadas.
    @Column(name = "a_usuario_id", nullable = false)
    private UUID paraUsuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoAmistad estado = EstadoAmistad.PENDIENTE;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud = LocalDateTime.now();

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    public Amistad(UUID deUsuarioId, UUID paraUsuarioId) {
        this.deUsuarioId = deUsuarioId;
        this.paraUsuarioId = paraUsuarioId;
    }

    public enum EstadoAmistad {
        PENDIENTE, ACEPTADA, RECHAZADA
    }
}

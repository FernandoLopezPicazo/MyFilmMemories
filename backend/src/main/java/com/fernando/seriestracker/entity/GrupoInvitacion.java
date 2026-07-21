package com.fernando.seriestracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "grupo_invitaciones")
@Data
@NoArgsConstructor
public class GrupoInvitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "grupo_invitacion_seq")
    @SequenceGenerator(name = "grupo_invitacion_seq", sequenceName = "GRUPO_INVITACION_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "grupo_id", nullable = false)
    private Long grupoId;

    @Column(name = "de_usuario_id", nullable = false)
    private UUID deUsuarioId;

    @Column(name = "a_usuario_id", nullable = false)
    private UUID paraUsuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoInvitacion estado = EstadoInvitacion.PENDIENTE;

    @Column(name = "fecha_invitacion", nullable = false)
    private LocalDateTime fechaInvitacion = LocalDateTime.now();

    public GrupoInvitacion(Long grupoId, UUID deUsuarioId, UUID paraUsuarioId) {
        this.grupoId = grupoId;
        this.deUsuarioId = deUsuarioId;
        this.paraUsuarioId = paraUsuarioId;
    }

    public enum EstadoInvitacion {
        PENDIENTE, ACEPTADA, RECHAZADA
    }
}

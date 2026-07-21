package com.fernando.seriestracker.repository;

import com.fernando.seriestracker.entity.GrupoInvitacion;
import com.fernando.seriestracker.entity.GrupoInvitacion.EstadoInvitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GrupoInvitacionRepository extends JpaRepository<GrupoInvitacion, Long> {

    List<GrupoInvitacion> findByParaUsuarioIdAndEstado(UUID paraUsuarioId, EstadoInvitacion estado);

    Optional<GrupoInvitacion> findByGrupoIdAndParaUsuarioIdAndEstado(Long grupoId, UUID paraUsuarioId, EstadoInvitacion estado);
}

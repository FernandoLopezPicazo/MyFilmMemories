package com.fernando.seriestracker.repository;

import com.fernando.seriestracker.entity.GrupoItemOpinion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GrupoItemOpinionRepository extends JpaRepository<GrupoItemOpinion, Long> {

    List<GrupoItemOpinion> findByGrupoItemId(Long grupoItemId);

    Optional<GrupoItemOpinion> findByGrupoItemIdAndUsuarioId(Long grupoItemId, UUID usuarioId);
}

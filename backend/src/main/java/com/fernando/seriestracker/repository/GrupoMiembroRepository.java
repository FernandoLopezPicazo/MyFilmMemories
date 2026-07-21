package com.fernando.seriestracker.repository;

import com.fernando.seriestracker.entity.GrupoMiembro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GrupoMiembroRepository extends JpaRepository<GrupoMiembro, Long> {

    List<GrupoMiembro> findByUsuarioId(UUID usuarioId);

    List<GrupoMiembro> findByGrupoId(Long grupoId);

    Optional<GrupoMiembro> findByGrupoIdAndUsuarioId(Long grupoId, UUID usuarioId);

    boolean existsByGrupoIdAndUsuarioId(Long grupoId, UUID usuarioId);

    void deleteByGrupoIdAndUsuarioId(Long grupoId, UUID usuarioId);
}

package com.fernando.seriestracker.repository;

import com.fernando.seriestracker.entity.GrupoSaga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrupoSagaRepository extends JpaRepository<GrupoSaga, Long> {
    List<GrupoSaga> findByGrupoId(Long grupoId);

    Optional<GrupoSaga> findByIdAndGrupoId(Long id, Long grupoId);
}

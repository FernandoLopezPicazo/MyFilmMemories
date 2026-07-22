package com.fernando.seriestracker.repository;

import com.fernando.seriestracker.entity.GrupoItem;
import com.fernando.seriestracker.entity.GrupoItem.TipoGrupoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrupoItemRepository extends JpaRepository<GrupoItem, Long> {

    List<GrupoItem> findByGrupoIdAndTipo(Long grupoId, TipoGrupoItem tipo);

    Optional<GrupoItem> findByIdAndGrupoId(Long id, Long grupoId);

    List<GrupoItem> findBySagaId(Long sagaId);
}

package com.fernando.seriestracker.repository;

import com.fernando.seriestracker.entity.Saga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SagaRepository extends JpaRepository<Saga, Long> {
    List<Saga> findByUsuarioId(UUID usuarioId);

    Optional<Saga> findByIdAndUsuarioId(Long id, UUID usuarioId);
}

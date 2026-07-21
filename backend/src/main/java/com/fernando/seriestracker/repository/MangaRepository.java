package com.fernando.seriestracker.repository;

import com.fernando.seriestracker.entity.Manga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MangaRepository extends JpaRepository<Manga, Long> {
    List<Manga> findByEstado(Manga.EstadoManga estado);

    List<Manga> findByUsuarioId(UUID usuarioId);

    List<Manga> findByUsuarioIdAndEstado(UUID usuarioId, Manga.EstadoManga estado);

    Optional<Manga> findByIdAndUsuarioId(Long id, UUID usuarioId);

    boolean existsByIdAndUsuarioId(Long id, UUID usuarioId);
}

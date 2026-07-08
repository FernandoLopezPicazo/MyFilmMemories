package com.fernando.seriestracker.repository;

import com.fernando.seriestracker.entity.Manga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MangaRepository extends JpaRepository<Manga, Long> {
    List<Manga> findByEstado(Manga.EstadoManga estado);
}

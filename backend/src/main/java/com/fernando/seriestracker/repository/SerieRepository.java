package com.fernando.seriestracker.repository;

import com.fernando.seriestracker.entity.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Spring Data JPA genera automáticamente la implementación de esta interfaz.
 * JpaRepository<Serie, Long>:
 *   - Serie → entidad que gestiona
 *   - Long  → tipo del @Id
 *
 * Métodos heredados gratis: findAll(), findById(), save(), deleteById()...
 *
 * findByEstado() es un "derived query": Spring lee el nombre y genera
 * automáticamente: SELECT * FROM series WHERE estado = ?
 */
@Repository
public interface SerieRepository extends JpaRepository<Serie, Long> {

    List<Serie> findByEstado(Serie.EstadoSerie estado);
}

package com.fernando.seriestracker.repository;

import com.fernando.seriestracker.entity.Pelicula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Spring Data JPA genera automáticamente la implementación de esta interfaz.
 * JpaRepository<Pelicula, Long>:
 *   - Pelicula → entidad que gestiona
 *   - Long  → tipo del @Id
 *
 * Métodos heredados gratis: findAll(), findById(), save(), deleteById()...
 *
 * findByEstado() es un "derived query": Spring lee el nombre y genera
 * automáticamente: SELECT * FROM peliculas WHERE estado = ?
 */
@Repository
public interface PeliculaRepository extends JpaRepository<Pelicula, Long> {

    List<Pelicula> findByEstado(Pelicula.EstadoPelicula estado);

    // Solo películas sueltas (sin saga) por estado
    List<Pelicula> findByEstadoAndSagaIdIsNull(Pelicula.EstadoPelicula estado);

    // Todas las películas de una saga
    List<Pelicula> findBySagaId(Long sagaId);
}

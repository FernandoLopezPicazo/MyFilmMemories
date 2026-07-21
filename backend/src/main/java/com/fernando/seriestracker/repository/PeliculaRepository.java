package com.fernando.seriestracker.repository;

import com.fernando.seriestracker.entity.Pelicula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
 *
 * Los métodos *ByUsuarioId* filtran siempre por el propietario del registro:
 * cada usuario solo debe ver/editar sus propias películas.
 */
@Repository
public interface PeliculaRepository extends JpaRepository<Pelicula, Long> {

    List<Pelicula> findByEstado(Pelicula.EstadoPelicula estado);

    // Solo películas sueltas (sin saga) por estado
    List<Pelicula> findByEstadoAndSagaIdIsNull(Pelicula.EstadoPelicula estado);

    // Todas las películas de una saga
    List<Pelicula> findBySagaId(Long sagaId);

    List<Pelicula> findByUsuarioIdAndEstadoAndSagaIdIsNull(UUID usuarioId, Pelicula.EstadoPelicula estado);

    List<Pelicula> findByUsuarioIdAndSagaIdIsNull(UUID usuarioId);

    List<Pelicula> findBySagaIdAndUsuarioId(Long sagaId, UUID usuarioId);

    Optional<Pelicula> findByIdAndUsuarioId(Long id, UUID usuarioId);

    boolean existsByIdAndUsuarioId(Long id, UUID usuarioId);
}

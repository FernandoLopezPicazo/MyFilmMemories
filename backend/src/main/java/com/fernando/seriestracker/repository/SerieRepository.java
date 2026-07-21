package com.fernando.seriestracker.repository;

import com.fernando.seriestracker.entity.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
 *
 * Los métodos *ByUsuarioId* filtran siempre por el propietario del registro:
 * cada usuario solo debe ver/editar sus propias series.
 */
@Repository
public interface SerieRepository extends JpaRepository<Serie, Long> {

    List<Serie> findByEstado(Serie.EstadoSerie estado);

    List<Serie> findByUsuarioId(UUID usuarioId);

    List<Serie> findByUsuarioIdAndEstado(UUID usuarioId, Serie.EstadoSerie estado);

    Optional<Serie> findByIdAndUsuarioId(Long id, UUID usuarioId);

    boolean existsByIdAndUsuarioId(Long id, UUID usuarioId);

    // Para ver la colección de un amigo: solo lo que no haya marcado como oculto
    List<Serie> findByUsuarioIdAndOcultoParaAmigosFalse(UUID usuarioId);
}

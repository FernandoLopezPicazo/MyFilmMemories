package com.fernando.seriestracker.repository;

import com.fernando.seriestracker.entity.Amistad;
import com.fernando.seriestracker.entity.Amistad.EstadoAmistad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AmistadRepository extends JpaRepository<Amistad, Long> {

    List<Amistad> findByParaUsuarioIdAndEstado(UUID paraUsuarioId, EstadoAmistad estado);

    List<Amistad> findByDeUsuarioIdAndEstado(UUID deUsuarioId, EstadoAmistad estado);

    @Query("select a from Amistad a where (a.deUsuarioId = :usuarioId or a.paraUsuarioId = :usuarioId) and a.estado = 'ACEPTADA'")
    List<Amistad> findAmistadesAceptadas(@Param("usuarioId") UUID usuarioId);

    @Query("select a from Amistad a where " +
            "((a.deUsuarioId = :usuario1 and a.paraUsuarioId = :usuario2) or " +
            "(a.deUsuarioId = :usuario2 and a.paraUsuarioId = :usuario1))")
    Optional<Amistad> findEntreUsuarios(@Param("usuario1") UUID usuario1, @Param("usuario2") UUID usuario2);

    @Query("select count(a) > 0 from Amistad a where " +
            "((a.deUsuarioId = :usuario1 and a.paraUsuarioId = :usuario2) or " +
            "(a.deUsuarioId = :usuario2 and a.paraUsuarioId = :usuario1)) and a.estado = 'ACEPTADA'")
    boolean sonAmigos(@Param("usuario1") UUID usuario1, @Param("usuario2") UUID usuario2);
}

package com.fernando.seriestracker.repository;

import com.fernando.seriestracker.entity.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, UUID> {

    Optional<Perfil> findByEmailIgnoreCase(String email);
}

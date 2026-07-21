package com.fernando.seriestracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/*
 * Espejo local y ligero de auth.users de Supabase: solo guarda el email
 * asociado a cada usuario_id. Se necesita porque el frontend no puede
 * consultar la tabla de Supabase Auth directamente — así el backend puede
 * buscar amigos por email sin llamar a la API de administración de Supabase.
 * Se sincroniza (upsert) una vez tras cada login desde AuthService.
 */
@Entity
@Table(name = "perfiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Perfil {

    @Id
    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(nullable = false, unique = true)
    private String email;
}

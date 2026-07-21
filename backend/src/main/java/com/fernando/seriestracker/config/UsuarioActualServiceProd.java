package com.fernando.seriestracker.config;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.UUID;

/*
 * El "sub" del JWT de Supabase Auth ES el UUID del usuario en la tabla
 * auth.users — es el mismo valor que guardamos como usuarioId en
 * Serie/Pelicula/Manga/Saga. SecurityConfig (activo solo en profile "prod")
 * ya ha validado el JWT antes de que se llegue aquí.
 */
@Service
@Profile("prod")
public class UsuarioActualServiceProd implements UsuarioActualService {

    @Override
    public UUID obtenerId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return UUID.fromString(jwt.getSubject());
    }
}

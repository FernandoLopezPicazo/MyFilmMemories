package com.fernando.seriestracker.config;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.UUID;

/*
 * Usado por el instalador Electron / desarrollo local con H2 (profile "dev",
 * o cualquier profile distinto de "prod"). No hay login ni usuarios reales
 * en ese flujo — sigue siendo una app de un único dueño local — así que
 * devolvemos un id fijo para que Serie/Pelicula/Manga/Saga queden todas
 * bajo el mismo "propietario" sin cambiar el comportamiento que ya conocías.
 */
@Service
@Profile("!prod")
public class UsuarioActualServiceDev implements UsuarioActualService {

    public static final UUID USUARIO_LOCAL = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Override
    public UUID obtenerId() {
        return USUARIO_LOCAL;
    }
}

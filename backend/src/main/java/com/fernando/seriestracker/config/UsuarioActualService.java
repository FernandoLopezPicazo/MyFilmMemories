package com.fernando.seriestracker.config;

import java.util.UUID;

/*
 * Devuelve el id del usuario "dueño" de los datos que se están consultando.
 *
 * Tiene dos implementaciones según el profile activo:
 *   - UsuarioActualServiceProd (profile "prod", nube): lee el usuario real
 *     autenticado desde el JWT validado por Spring Security.
 *   - UsuarioActualServiceDev (cualquier otro profile, incluido el que usa
 *     el instalador Electron local): devuelve un id fijo — el instalador de
 *     escritorio no tiene login ni concepto de usuario, sigue siendo de un
 *     único dueño local, así que todo su contenido queda bajo ese id fijo
 *     sin que el usuario note ningún cambio de comportamiento.
 */
public interface UsuarioActualService {
    UUID obtenerId();
}

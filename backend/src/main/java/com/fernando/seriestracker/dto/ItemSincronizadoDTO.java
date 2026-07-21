package com.fernando.seriestracker.dto;

/*
 * Envuelve un ítem ya fusionado por la sincronización, junto con el id LOCAL
 * (del escritorio) que traía en la petición — si venía de la nube y no del
 * lote local, localId es null. El cliente usa localId para saber si debe
 * ACTUALIZAR una fila local existente o INSERTAR una nueva.
 */
public record ItemSincronizadoDTO<T>(Long localId, T item) {
}

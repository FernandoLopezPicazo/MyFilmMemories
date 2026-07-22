package com.fernando.seriestracker.service;

import com.fernando.seriestracker.config.UsuarioActualService;
import com.fernando.seriestracker.dto.ItemSincronizadoDTO;
import com.fernando.seriestracker.entity.Manga;
import com.fernando.seriestracker.entity.Pelicula;
import com.fernando.seriestracker.entity.Serie;
import com.fernando.seriestracker.repository.MangaRepository;
import com.fernando.seriestracker.repository.PeliculaRepository;
import com.fernando.seriestracker.repository.SerieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/*
 * CONCEPTO CLAVE — Sincronización escritorio↔nube:
 *
 * El escritorio (H2, sin login) y la nube (Postgres, con tu cuenta) tienen
 * contadores de id independientes: "serie nº 5" en un sitio no tiene
 * relación con "serie nº 5" en el otro. Por eso cada Serie/Pelicula/Manga
 * lleva un "syncId" (UUID) estable que identifica el título entre ambas
 * bases de datos, generado la primera vez que se crea el título (ver
 * SerieService.crear() y equivalentes).
 *
 * Esta clase recibe el lote de títulos tal y como están en el lado que
 * llama (local o nube — el mismo código sirve para los dos, ver
 * SincronizacionController) y lo fusiona contra lo que ya existe para ese
 * usuario:
 *   - Si el syncId ya existe → gana el más reciente por "actualizadoEn".
 *   - Si no hay match por syncId (porque es null, o porque el otro lado aún
 *     no conoce el syncId que se le asignó en una pasada anterior) → se
 *     empareja con lo existente por título exacto, como mejor esfuerzo. Esto
 *     es necesario incluso cuando el syncId entrante NO es null: un título
 *     "legacy" recién emparejado en UN lado (que ya tiene syncId asignado)
 *     todavía no lo tiene reflejado en el OTRO lado hasta que complete su
 *     propio primer emparejamiento — si en ese momento solo se intentara el
 *     título quedaría excluido por ya tener syncId, y el título acabaría
 *     duplicándose.
 *   - Si no hay ningún emparejamiento → es un título nuevo, se crea.
 *   - Lo que exista en este lado y NO viniera en el lote también se
 *     devuelve, para que el otro lado lo descargue.
 *
 * Los borrados NO se sincronizan — si quieres quitar un título de los dos
 * sitios, tienes que borrarlo en cada uno por separado. Propagar borrados
 * automáticamente es mucho más fácil que se lleve por delante datos que sí
 * querías conservar, así que se deja fuera de esta primera versión.
 */
@Service
@RequiredArgsConstructor
public class SincronizacionService {

    private final SerieRepository serieRepository;
    private final PeliculaRepository peliculaRepository;
    private final MangaRepository mangaRepository;
    private final UsuarioActualService usuarioActual;

    @Transactional
    public List<ItemSincronizadoDTO<Serie>> sincronizarSeries(List<Serie> lote) {
        UUID yo = usuarioActual.obtenerId();
        List<Serie> existentes = serieRepository.findByUsuarioId(yo);

        Map<UUID, Serie> porSyncId = new HashMap<>();
        Map<String, Serie> porTitulo = new HashMap<>();
        for (Serie s : existentes) {
            if (s.getSyncId() != null) porSyncId.put(s.getSyncId(), s);
            porTitulo.putIfAbsent(clavePorTitulo(s.getTitulo()), s);
        }

        Set<Long> idsUsados = new HashSet<>();
        List<ItemSincronizadoDTO<Serie>> resultado = new ArrayList<>();

        for (Serie local : lote) {
            Long localId = local.getId();
            Serie existente = local.getSyncId() != null ? porSyncId.get(local.getSyncId()) : null;
            if (existente == null) {
                Serie porTituloCandidata = porTitulo.get(clavePorTitulo(local.getTitulo()));
                if (porTituloCandidata != null && !idsUsados.contains(porTituloCandidata.getId())) {
                    existente = porTituloCandidata;
                }
            }

            if (existente != null) {
                if (esMasReciente(local.getActualizadoEn(), existente.getActualizadoEn())) {
                    existente.setTitulo(local.getTitulo());
                    existente.setDescripcion(local.getDescripcion());
                    existente.setImagenUrl(local.getImagenUrl());
                    existente.setNombrePersona1(local.getNombrePersona1());
                    existente.setPersonajeFavorito(local.getPersonajeFavorito());
                    existente.setPersonajeOdiado(local.getPersonajeOdiado());
                    existente.setNota(local.getNota());
                    existente.setNombrePersona2(local.getNombrePersona2());
                    existente.setPersonajeFavorito2(local.getPersonajeFavorito2());
                    existente.setPersonajeOdiado2(local.getPersonajeOdiado2());
                    existente.setNota2(local.getNota2());
                    existente.setEstado(local.getEstado());
                    existente.setFechaVista(local.getFechaVista());
                    existente.setTemporadaActual(local.getTemporadaActual());
                    existente.setEpisodioActual(local.getEpisodioActual());
                    existente.setOcultoParaAmigos(local.isOcultoParaAmigos());
                    existente.setGeneros(local.getGeneros());
                    existente.setActualizadoEn(local.getActualizadoEn());
                }
                if (existente.getSyncId() == null) existente.setSyncId(UUID.randomUUID());
                serieRepository.save(existente);
                idsUsados.add(existente.getId());
                resultado.add(new ItemSincronizadoDTO<>(localId, existente));
            } else {
                local.setId(null);
                local.setUsuarioId(yo);
                if (local.getSyncId() == null) local.setSyncId(UUID.randomUUID());
                if (local.getActualizadoEn() == null) local.setActualizadoEn(LocalDateTime.now(java.time.ZoneOffset.UTC));
                Serie creada = serieRepository.save(local);
                resultado.add(new ItemSincronizadoDTO<>(localId, creada));
            }
        }

        for (Serie existente : existentes) {
            if (!idsUsados.contains(existente.getId())) {
                if (existente.getSyncId() == null) {
                    existente.setSyncId(UUID.randomUUID());
                    serieRepository.save(existente);
                }
                resultado.add(new ItemSincronizadoDTO<>(null, existente));
            }
        }

        return resultado;
    }

    @Transactional
    public List<ItemSincronizadoDTO<Pelicula>> sincronizarPeliculas(List<Pelicula> lote) {
        UUID yo = usuarioActual.obtenerId();
        List<Pelicula> existentes = peliculaRepository.findByUsuarioIdAndSagaIdIsNull(yo);

        Map<UUID, Pelicula> porSyncId = new HashMap<>();
        Map<String, Pelicula> porTitulo = new HashMap<>();
        for (Pelicula p : existentes) {
            if (p.getSyncId() != null) porSyncId.put(p.getSyncId(), p);
            porTitulo.putIfAbsent(clavePorTitulo(p.getTitulo()), p);
        }

        Set<Long> idsUsados = new HashSet<>();
        List<ItemSincronizadoDTO<Pelicula>> resultado = new ArrayList<>();

        for (Pelicula local : lote) {
            Long localId = local.getId();
            Pelicula existente = local.getSyncId() != null ? porSyncId.get(local.getSyncId()) : null;
            if (existente == null) {
                Pelicula porTituloCandidata = porTitulo.get(clavePorTitulo(local.getTitulo()));
                if (porTituloCandidata != null && !idsUsados.contains(porTituloCandidata.getId())) {
                    existente = porTituloCandidata;
                }
            }

            if (existente != null) {
                if (esMasReciente(local.getActualizadoEn(), existente.getActualizadoEn())) {
                    existente.setTitulo(local.getTitulo());
                    existente.setDescripcion(local.getDescripcion());
                    existente.setImagenUrl(local.getImagenUrl());
                    existente.setNombrePersona1(local.getNombrePersona1());
                    existente.setPersonajeFavorito(local.getPersonajeFavorito());
                    existente.setPersonajeOdiado(local.getPersonajeOdiado());
                    existente.setNota(local.getNota());
                    existente.setNombrePersona2(local.getNombrePersona2());
                    existente.setPersonajeFavorito2(local.getPersonajeFavorito2());
                    existente.setPersonajeOdiado2(local.getPersonajeOdiado2());
                    existente.setNota2(local.getNota2());
                    existente.setEstado(local.getEstado());
                    existente.setFechaVista(local.getFechaVista());
                    existente.setDuracionMinutos(local.getDuracionMinutos());
                    existente.setOcultoParaAmigos(local.isOcultoParaAmigos());
                    existente.setGeneros(local.getGeneros());
                    existente.setActualizadoEn(local.getActualizadoEn());
                    // sagaId NO se toca: las sagas quedan fuera de la sincronización.
                }
                if (existente.getSyncId() == null) existente.setSyncId(UUID.randomUUID());
                peliculaRepository.save(existente);
                idsUsados.add(existente.getId());
                resultado.add(new ItemSincronizadoDTO<>(localId, existente));
            } else {
                local.setId(null);
                local.setUsuarioId(yo);
                local.setSagaId(null);
                if (local.getSyncId() == null) local.setSyncId(UUID.randomUUID());
                if (local.getActualizadoEn() == null) local.setActualizadoEn(LocalDateTime.now(java.time.ZoneOffset.UTC));
                Pelicula creada = peliculaRepository.save(local);
                resultado.add(new ItemSincronizadoDTO<>(localId, creada));
            }
        }

        for (Pelicula existente : existentes) {
            if (!idsUsados.contains(existente.getId())) {
                if (existente.getSyncId() == null) {
                    existente.setSyncId(UUID.randomUUID());
                    peliculaRepository.save(existente);
                }
                resultado.add(new ItemSincronizadoDTO<>(null, existente));
            }
        }

        return resultado;
    }

    @Transactional
    public List<ItemSincronizadoDTO<Manga>> sincronizarMangas(List<Manga> lote) {
        UUID yo = usuarioActual.obtenerId();
        List<Manga> existentes = mangaRepository.findByUsuarioId(yo);

        Map<UUID, Manga> porSyncId = new HashMap<>();
        Map<String, Manga> porTitulo = new HashMap<>();
        for (Manga m : existentes) {
            if (m.getSyncId() != null) porSyncId.put(m.getSyncId(), m);
            porTitulo.putIfAbsent(clavePorTitulo(m.getTitulo()), m);
        }

        Set<Long> idsUsados = new HashSet<>();
        List<ItemSincronizadoDTO<Manga>> resultado = new ArrayList<>();

        for (Manga local : lote) {
            Long localId = local.getId();
            Manga existente = local.getSyncId() != null ? porSyncId.get(local.getSyncId()) : null;
            if (existente == null) {
                Manga porTituloCandidata = porTitulo.get(clavePorTitulo(local.getTitulo()));
                if (porTituloCandidata != null && !idsUsados.contains(porTituloCandidata.getId())) {
                    existente = porTituloCandidata;
                }
            }

            if (existente != null) {
                if (esMasReciente(local.getActualizadoEn(), existente.getActualizadoEn())) {
                    existente.setTitulo(local.getTitulo());
                    existente.setDescripcion(local.getDescripcion());
                    existente.setImagenUrl(local.getImagenUrl());
                    existente.setCapituloActual(local.getCapituloActual());
                    existente.setUrlLectura(local.getUrlLectura());
                    existente.setNombrePersona1(local.getNombrePersona1());
                    existente.setPersonajeFavorito(local.getPersonajeFavorito());
                    existente.setPersonajeOdiado(local.getPersonajeOdiado());
                    existente.setNota(local.getNota());
                    existente.setNombrePersona2(local.getNombrePersona2());
                    existente.setPersonajeFavorito2(local.getPersonajeFavorito2());
                    existente.setPersonajeOdiado2(local.getPersonajeOdiado2());
                    existente.setNota2(local.getNota2());
                    existente.setEstado(local.getEstado());
                    existente.setFechaFinalizado(local.getFechaFinalizado());
                    existente.setOcultoParaAmigos(local.isOcultoParaAmigos());
                    existente.setGeneros(local.getGeneros());
                    existente.setActualizadoEn(local.getActualizadoEn());
                }
                if (existente.getSyncId() == null) existente.setSyncId(UUID.randomUUID());
                mangaRepository.save(existente);
                idsUsados.add(existente.getId());
                resultado.add(new ItemSincronizadoDTO<>(localId, existente));
            } else {
                local.setId(null);
                local.setUsuarioId(yo);
                if (local.getSyncId() == null) local.setSyncId(UUID.randomUUID());
                if (local.getActualizadoEn() == null) local.setActualizadoEn(LocalDateTime.now(java.time.ZoneOffset.UTC));
                Manga creado = mangaRepository.save(local);
                resultado.add(new ItemSincronizadoDTO<>(localId, creado));
            }
        }

        for (Manga existente : existentes) {
            if (!idsUsados.contains(existente.getId())) {
                if (existente.getSyncId() == null) {
                    existente.setSyncId(UUID.randomUUID());
                    mangaRepository.save(existente);
                }
                resultado.add(new ItemSincronizadoDTO<>(null, existente));
            }
        }

        return resultado;
    }

    private String clavePorTitulo(String titulo) {
        return titulo == null ? "" : titulo.trim().toLowerCase();
    }

    // El lado que no manda fecha (por ejemplo un título recién creado que
    // todavía no ha pasado por crear()) nunca "gana" frente a uno que sí la
    // tiene — evita sobreescribir datos buenos con datos sin fecha.
    private boolean esMasReciente(LocalDateTime candidata, LocalDateTime actual) {
        if (candidata == null) return false;
        if (actual == null) return true;
        return candidata.isAfter(actual);
    }
}

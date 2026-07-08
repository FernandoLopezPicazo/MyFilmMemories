package com.fernando.seriestracker.service;

import com.fernando.seriestracker.entity.Manga;
import com.fernando.seriestracker.repository.MangaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MangaService {

    private final MangaRepository mangaRepository;

    @Transactional(readOnly = true)
    public List<Manga> obtenerTodos() {
        return mangaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Manga> obtenerPorEstado(Manga.EstadoManga estado) {
        return mangaRepository.findByEstado(estado);
    }

    @Transactional
    public Manga crear(Manga manga) {
        manga.setEstado(Manga.EstadoManga.PENDIENTE);
        manga.setNota(null);
        manga.setFechaFinalizado(null);
        manga.setCapituloActual(null);
        return mangaRepository.save(manga);
    }

    @Transactional
    public void marcarComoEnProceso(Long id) {
        Manga manga = mangaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe un manga con id: " + id));
        manga.setEstado(Manga.EstadoManga.EN_PROCESO);
        if (manga.getCapituloActual() == null) manga.setCapituloActual(1);
        mangaRepository.save(manga);
    }

    @Transactional
    public void actualizarProgreso(Long id, Integer capitulo, String urlLectura) {
        Manga manga = mangaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe un manga con id: " + id));
        if (capitulo != null) manga.setCapituloActual(capitulo);
        if (urlLectura != null) manga.setUrlLectura(urlLectura);
        mangaRepository.save(manga);
    }

    @Transactional
    public Manga finalizar(Long id, Manga datos, Integer nota) {
        Manga manga = mangaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe un manga con id: " + id));

        manga.setEstado(Manga.EstadoManga.FINALIZADO);
        manga.setFechaFinalizado(java.time.LocalDate.now());
        if (nota != null) manga.setNota(nota);
        manga.setDescripcion(datos.getDescripcion());
        if (datos.getImagenUrl() != null) manga.setImagenUrl(datos.getImagenUrl());
        manga.setNombrePersona1(datos.getNombrePersona1());
        manga.setPersonajeFavorito(datos.getPersonajeFavorito());
        manga.setPersonajeOdiado(datos.getPersonajeOdiado());
        manga.setNombrePersona2(datos.getNombrePersona2());
        manga.setPersonajeFavorito2(datos.getPersonajeFavorito2());
        manga.setPersonajeOdiado2(datos.getPersonajeOdiado2());
        manga.setNota2(datos.getNota2());
        if (datos.getUrlLectura() != null) manga.setUrlLectura(datos.getUrlLectura());
        manga.setGeneros(datos.getGeneros() != null ? datos.getGeneros() : new java.util.ArrayList<>());
        return mangaRepository.save(manga);
    }

    @Transactional
    public Manga editar(Long id, Manga datos) {
        Manga manga = mangaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe un manga con id: " + id));

        manga.setTitulo(datos.getTitulo());
        manga.setDescripcion(datos.getDescripcion());
        if (datos.getImagenUrl() != null) manga.setImagenUrl(datos.getImagenUrl());
        manga.setNombrePersona1(datos.getNombrePersona1());
        manga.setPersonajeFavorito(datos.getPersonajeFavorito());
        manga.setPersonajeOdiado(datos.getPersonajeOdiado());
        manga.setNombrePersona2(datos.getNombrePersona2());
        manga.setPersonajeFavorito2(datos.getPersonajeFavorito2());
        manga.setPersonajeOdiado2(datos.getPersonajeOdiado2());
        manga.setNota2(datos.getNota2());
        if (datos.getNota() != null) manga.setNota(datos.getNota());
        if (datos.getCapituloActual() != null) manga.setCapituloActual(datos.getCapituloActual());
        if (datos.getUrlLectura() != null) manga.setUrlLectura(datos.getUrlLectura());
        manga.setGeneros(datos.getGeneros() != null ? datos.getGeneros() : new java.util.ArrayList<>());
        return mangaRepository.save(manga);
    }

    @Transactional
    public void marcarComoPendiente(Long id) {
        Manga manga = mangaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe un manga con id: " + id));
        manga.setEstado(Manga.EstadoManga.PENDIENTE);
        mangaRepository.save(manga);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!mangaRepository.existsById(id)) {
            throw new IllegalArgumentException("No existe un manga con id: " + id);
        }
        mangaRepository.deleteById(id);
    }
}

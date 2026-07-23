package com.fernando.seriestracker.service;

import com.fernando.seriestracker.config.UsuarioActualService;
import com.fernando.seriestracker.dto.ItemProgramadoDTO;
import com.fernando.seriestracker.entity.Manga;
import com.fernando.seriestracker.entity.Pelicula;
import com.fernando.seriestracker.entity.Serie;
import com.fernando.seriestracker.repository.MangaRepository;
import com.fernando.seriestracker.repository.PeliculaRepository;
import com.fernando.seriestracker.repository.SerieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
 * Junta series, peliculas y mangas marcados "en emision" con un dia de la
 * semana asignado, en una sola lista para el horario semanal. No filtra
 * por semana del mes aqui — eso lo hace el frontend, que ya sabe que
 * semana del mes es "hoy".
 */
@Service
@RequiredArgsConstructor
public class HorarioService {

    private final SerieRepository serieRepository;
    private final PeliculaRepository peliculaRepository;
    private final MangaRepository mangaRepository;
    private final UsuarioActualService usuarioActual;

    @Transactional(readOnly = true)
    public List<ItemProgramadoDTO> obtenerHorario() {
        UUID usuarioId = usuarioActual.obtenerId();
        List<ItemProgramadoDTO> resultado = new ArrayList<>();

        for (Serie s : serieRepository.findByUsuarioIdAndEnEmisionTrue(usuarioId)) {
            if (s.getDiaSemana() == null) continue;
            resultado.add(new ItemProgramadoDTO(s.getId(), "SERIE", s.getTitulo(), s.getImagenUrl(),
                    s.getDiaSemana().name(), s.getFrecuencia() != null ? s.getFrecuencia().name() : null,
                    s.getSemanaDelMes()));
        }
        for (Pelicula p : peliculaRepository.findByUsuarioIdAndEnEmisionTrue(usuarioId)) {
            if (p.getDiaSemana() == null) continue;
            resultado.add(new ItemProgramadoDTO(p.getId(), "PELICULA", p.getTitulo(), p.getImagenUrl(),
                    p.getDiaSemana().name(), p.getFrecuencia() != null ? p.getFrecuencia().name() : null,
                    p.getSemanaDelMes()));
        }
        for (Manga m : mangaRepository.findByUsuarioIdAndEnEmisionTrue(usuarioId)) {
            if (m.getDiaSemana() == null) continue;
            resultado.add(new ItemProgramadoDTO(m.getId(), "MANGA", m.getTitulo(), m.getImagenUrl(),
                    m.getDiaSemana().name(), m.getFrecuencia() != null ? m.getFrecuencia().name() : null,
                    m.getSemanaDelMes()));
        }
        return resultado;
    }
}

package com.fernando.seriestracker.service;

import com.fernando.seriestracker.config.UsuarioActualService;
import com.fernando.seriestracker.entity.Serie;
import com.fernando.seriestracker.repository.SerieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 * CONCEPTO CLAVE — Arquitectura en capas (Layered Architecture):
 *
 * Controller → Service → Repository → BD
 *
 * ¿Por qué existe la capa Service si el Controller podría llamar
 * directamente al Repository?
 *   1. Separación de responsabilidades: el Controller maneja HTTP
 *      (qué viene, qué se devuelve). El Service maneja NEGOCIO
 *      (qué reglas se aplican). El Repository maneja PERSISTENCIA.
 *   2. Reutilización: si mañana añades una interfaz CLI, llama al Service,
 *      no a otro Controller.
 *   3. Testabilidad: puedes testear el Service de forma independiente
 *      sin HTTP ni BD reales (mockeas el Repository).
 *
 * Pregunta de entrevista: "¿Por qué usas Service en vez de llamar al
 * Repository desde el Controller?"
 * → Separación de responsabilidades, reusabilidad y testabilidad.
 *
 * @Service: anotación de Spring que marca esta clase como un "bean" de
 * la capa de servicio. Spring la instanciará y la gestionará.
 * Internamente es igual que @Component pero semánticamente más expresivo.
 */
@Service
/*
 * @RequiredArgsConstructor (Lombok): genera un constructor con los campos
 * "final". Spring ve ese constructor y automáticamente inyecta el
 * SerieRepository — esto se llama INYECCIÓN DE DEPENDENCIAS por constructor,
 * que es la forma recomendada (vs @Autowired en campo).
 *
 * ¿Por qué constructor vs @Autowired en campo?
 *   - Con constructor, los campos son "final" → inmutables → más seguro.
 *   - Es más fácil de testear (puedes pasar un mock en el constructor).
 *   - Spring mismo lo recomienda desde la versión 4.3.
 */
@RequiredArgsConstructor
public class SerieService {

    private final SerieRepository serieRepository;
    private final UsuarioActualService usuarioActual;

    /*
     * @Transactional(readOnly = true): abre una transacción de BD de solo lectura.
     * "readOnly" permite optimizaciones: Hibernate no hace flush, algunos drivers
     * JDBC pueden usar réplicas de solo lectura. No cambia la lógica, pero
     * es buena práctica en métodos que solo leen.
     */
    @Transactional(readOnly = true)
    public List<Serie> obtenerTodas() {
        return serieRepository.findByUsuarioId(usuarioActual.obtenerId());
    }

    @Transactional(readOnly = true)
    public List<Serie> obtenerPorEstado(Serie.EstadoSerie estado) {
        return serieRepository.findByUsuarioIdAndEstado(usuarioActual.obtenerId(), estado);
    }

    /*
     * @Transactional (sin readOnly): abre una transacción completa.
     * Si el método lanza una excepción no chequeada (RuntimeException),
     * Hibernate hace ROLLBACK automáticamente.
     * Si todo va bien → COMMIT al final del método.
     *
     * Pregunta de entrevista: "¿Para qué sirve @Transactional?"
     * Garantiza que todas las operaciones del método forman una unidad
     * atómica (ACID): o todas tienen éxito o ninguna persiste.
     */
    @Transactional
    public Serie crear(Serie serie) {
        // Forzamos estado PENDIENTE y sin nota al crear — la regla de negocio
        // dice que la nota solo existe en series VISTAS.
        serie.setUsuarioId(usuarioActual.obtenerId());
        serie.setEstado(Serie.EstadoSerie.PENDIENTE);
        serie.setNota(null);
        serie.setFechaVista(null);
        return serieRepository.save(serie);
    }

    @Transactional
    public void marcarComoVista(Long id, Integer nota) {
        if (nota == null || nota < 0 || nota > 10) {
            throw new IllegalArgumentException("La nota debe estar entre 0 y 10");
        }

        /*
         * findById devuelve un Optional<Serie>.
         * Optional es un contenedor que puede tener valor o estar vacío —
         * evita los NullPointerException. orElseThrow lanza excepción si está vacío.
         *
         * Con Oracle usaríamos el stored procedure. Con H2 lo hacemos con JPA.
         */
        Serie serie = serieRepository.findByIdAndUsuarioId(id, usuarioActual.obtenerId())
                .orElseThrow(() -> new IllegalArgumentException("No existe una serie con id: " + id));

        serie.setEstado(Serie.EstadoSerie.VISTA);
        serie.setNota(nota);
        serie.setFechaVista(java.time.LocalDate.now());

        serieRepository.save(serie);
    }

    @Transactional
    public Serie editar(Long id, Serie datos) {
        Serie serie = serieRepository.findByIdAndUsuarioId(id, usuarioActual.obtenerId())
                .orElseThrow(() -> new IllegalArgumentException("No existe una serie con id: " + id));

        // Solo actualizamos los campos editables — estado y nota NO se tocan aquí
        serie.setTitulo(datos.getTitulo());
        serie.setDescripcion(datos.getDescripcion());
        if (datos.getImagenUrl() != null) {
            serie.setImagenUrl(datos.getImagenUrl());
        }
        serie.setNombrePersona1(datos.getNombrePersona1());
        serie.setPersonajeFavorito(datos.getPersonajeFavorito());
        serie.setPersonajeOdiado(datos.getPersonajeOdiado());
        serie.setNombrePersona2(datos.getNombrePersona2());
        serie.setPersonajeFavorito2(datos.getPersonajeFavorito2());
        serie.setPersonajeOdiado2(datos.getPersonajeOdiado2());
        serie.setNota2(datos.getNota2());
        serie.setGeneros(datos.getGeneros() != null ? datos.getGeneros() : new java.util.ArrayList<>());
        if (datos.getTemporadaActual() != null) serie.setTemporadaActual(datos.getTemporadaActual());
        if (datos.getEpisodioActual() != null) serie.setEpisodioActual(datos.getEpisodioActual());

        return serieRepository.save(serie);
    }

    @Transactional
    public void marcarComoPendiente(Long id) {
        Serie serie = serieRepository.findByIdAndUsuarioId(id, usuarioActual.obtenerId())
                .orElseThrow(() -> new IllegalArgumentException("No existe una serie con id: " + id));
        serie.setEstado(Serie.EstadoSerie.PENDIENTE);
        serieRepository.save(serie);
    }

    @Transactional
    public void marcarComoEnProceso(Long id) {
        Serie serie = serieRepository.findByIdAndUsuarioId(id, usuarioActual.obtenerId())
                .orElseThrow(() -> new IllegalArgumentException("No existe una serie con id: " + id));
        serie.setEstado(Serie.EstadoSerie.EN_PROCESO);
        if (serie.getTemporadaActual() == null) serie.setTemporadaActual(1);
        if (serie.getEpisodioActual() == null) serie.setEpisodioActual(1);
        serieRepository.save(serie);
    }

    @Transactional
    public void actualizarProgreso(Long id, Integer temporada, Integer episodio) {
        Serie serie = serieRepository.findByIdAndUsuarioId(id, usuarioActual.obtenerId())
                .orElseThrow(() -> new IllegalArgumentException("No existe una serie con id: " + id));
        if (temporada != null && temporada >= 1) serie.setTemporadaActual(temporada);
        if (episodio != null && episodio >= 1) serie.setEpisodioActual(episodio);
        serieRepository.save(serie);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!serieRepository.existsByIdAndUsuarioId(id, usuarioActual.obtenerId())) {
            throw new IllegalArgumentException("No existe una serie con id: " + id);
        }
        serieRepository.deleteById(id);
    }

    @Transactional
    public void alternarOcultoParaAmigos(Long id) {
        Serie serie = serieRepository.findByIdAndUsuarioId(id, usuarioActual.obtenerId())
                .orElseThrow(() -> new IllegalArgumentException("No existe una serie con id: " + id));
        serie.setOcultoParaAmigos(!serie.isOcultoParaAmigos());
        serieRepository.save(serie);
    }

    @Transactional(readOnly = true)
    public List<Serie> obtenerColeccionVisible(java.util.UUID usuarioId) {
        return serieRepository.findByUsuarioIdAndOcultoParaAmigosFalse(usuarioId);
    }
}

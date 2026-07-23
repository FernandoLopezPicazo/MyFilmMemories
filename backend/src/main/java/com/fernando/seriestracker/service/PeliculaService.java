package com.fernando.seriestracker.service;

import com.fernando.seriestracker.config.UsuarioActualService;
import com.fernando.seriestracker.entity.Pelicula;
import com.fernando.seriestracker.repository.PeliculaRepository;
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
public class PeliculaService {

    private final PeliculaRepository peliculaRepository;
    private final SagaService sagaService;
    private final UsuarioActualService usuarioActual;

    /*
     * @Transactional(readOnly = true): abre una transacción de BD de solo lectura.
     * "readOnly" permite optimizaciones: Hibernate no hace flush, algunos drivers
     * JDBC pueden usar réplicas de solo lectura. No cambia la lógica, pero
     * es buena práctica en métodos que solo leen.
     */
    @Transactional(readOnly = true)
    public List<Pelicula> obtenerTodas() {
        return peliculaRepository.findByUsuarioIdAndSagaIdIsNull(usuarioActual.obtenerId());
    }

    @Transactional(readOnly = true)
    public List<Pelicula> obtenerPorEstado(Pelicula.EstadoPelicula estado) {
        // Solo devuelve películas sueltas (sin saga) — las de saga se gestionan desde SagaController
        return peliculaRepository.findByUsuarioIdAndEstadoAndSagaIdIsNull(usuarioActual.obtenerId(), estado);
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
    public Pelicula crear(Pelicula pelicula) {
        // Forzamos estado PENDIENTE y sin nota al crear — la regla de negocio
        // dice que la nota solo existe en pelicula VISTAS.
        pelicula.setUsuarioId(usuarioActual.obtenerId());
        pelicula.setEstado(Pelicula.EstadoPelicula.PENDIENTE);
        pelicula.setNota(null);
        pelicula.setFechaVista(null);
        pelicula.setSyncId(java.util.UUID.randomUUID());
        pelicula.setActualizadoEn(java.time.LocalDateTime.now(java.time.ZoneOffset.UTC));
        return peliculaRepository.save(pelicula);
    }

    @Transactional
    public void marcarComoVista(Long id, Integer nota) {
        if (nota == null || nota < 0 || nota > 10) {
            throw new IllegalArgumentException("La nota debe estar entre 0 y 10");
        }

        /*
         * findById devuelve un Optional<Pelicula>.
         * Optional es un contenedor que puede tener valor o estar vacío —
         * evita los NullPointerException. orElseThrow lanza excepción si está vacío.
         *
         * Con Oracle usaríamos el stored procedure. Con H2 lo hacemos con JPA.
         */
        Pelicula pelicula = peliculaRepository.findByIdAndUsuarioId(id, usuarioActual.obtenerId())
                .orElseThrow(() -> new IllegalArgumentException("No existe una pelicula con id: " + id));

        pelicula.setEstado(Pelicula.EstadoPelicula.VISTA);
        pelicula.setNota(nota);
        pelicula.setFechaVista(java.time.LocalDate.now());
        pelicula.setActualizadoEn(java.time.LocalDateTime.now(java.time.ZoneOffset.UTC));

        peliculaRepository.save(pelicula);

        // Si pertenece a una saga, recalcular su estado
        if (pelicula.getSagaId() != null) {
            sagaService.recalcularEstado(pelicula.getSagaId());
        }
    }

    @Transactional
    public Pelicula editar(Long id, Pelicula datos) {
        Pelicula pelicula = peliculaRepository.findByIdAndUsuarioId(id, usuarioActual.obtenerId())
                .orElseThrow(() -> new IllegalArgumentException("No existe una pelicula con id: " + id));

        // Solo actualizamos los campos editables — estado y nota NO se tocan aquí
        pelicula.setTitulo(datos.getTitulo());
        pelicula.setDescripcion(datos.getDescripcion());
        if (datos.getImagenUrl() != null) {
            pelicula.setImagenUrl(datos.getImagenUrl());
        }
        pelicula.setNombrePersona1(datos.getNombrePersona1());
        pelicula.setPersonajeFavorito(datos.getPersonajeFavorito());
        pelicula.setPersonajeOdiado(datos.getPersonajeOdiado());
        pelicula.setNombrePersona2(datos.getNombrePersona2());
        pelicula.setPersonajeFavorito2(datos.getPersonajeFavorito2());
        pelicula.setPersonajeOdiado2(datos.getPersonajeOdiado2());
        pelicula.setNota2(datos.getNota2());
        pelicula.setGeneros(datos.getGeneros() != null ? datos.getGeneros() : new java.util.ArrayList<>());
        if (datos.getDuracionMinutos() != null) pelicula.setDuracionMinutos(datos.getDuracionMinutos());
        pelicula.setEnEmision(datos.isEnEmision());
        pelicula.setFrecuencia(datos.getFrecuencia());
        pelicula.setDiaSemana(datos.getDiaSemana());
        pelicula.setSemanaDelMes(datos.getSemanaDelMes());
        pelicula.setActualizadoEn(java.time.LocalDateTime.now(java.time.ZoneOffset.UTC));

        return peliculaRepository.save(pelicula);
    }

    @Transactional
    public void marcarComoPendiente(Long id) {
        Pelicula pelicula = peliculaRepository.findByIdAndUsuarioId(id, usuarioActual.obtenerId())
                .orElseThrow(() -> new IllegalArgumentException("No existe una pelicula con id: " + id));
        pelicula.setEstado(Pelicula.EstadoPelicula.PENDIENTE);
        pelicula.setActualizadoEn(java.time.LocalDateTime.now(java.time.ZoneOffset.UTC));
        peliculaRepository.save(pelicula);

        if (pelicula.getSagaId() != null) {
            sagaService.recalcularEstado(pelicula.getSagaId());
        }
    }
    
    @Transactional
    public void eliminar(Long id) {
        if (!peliculaRepository.existsByIdAndUsuarioId(id, usuarioActual.obtenerId())) {
            throw new IllegalArgumentException("No existe una pelicula con id: " + id);
        }
        peliculaRepository.deleteById(id);
    }

    @Transactional
    public void alternarOcultoParaAmigos(Long id) {
        Pelicula pelicula = peliculaRepository.findByIdAndUsuarioId(id, usuarioActual.obtenerId())
                .orElseThrow(() -> new IllegalArgumentException("No existe una pelicula con id: " + id));
        pelicula.setOcultoParaAmigos(!pelicula.isOcultoParaAmigos());
        peliculaRepository.save(pelicula);
    }

    @Transactional(readOnly = true)
    public List<Pelicula> obtenerColeccionVisible(java.util.UUID usuarioId) {
        return peliculaRepository.findByUsuarioIdAndOcultoParaAmigosFalse(usuarioId);
    }
}

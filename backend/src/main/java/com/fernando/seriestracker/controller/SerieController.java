package com.fernando.seriestracker.controller;

import com.fernando.seriestracker.entity.Serie;
import com.fernando.seriestracker.service.SerieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/*
 * CONCEPTO CLAVE — @RestController vs @Controller:
 *
 * @Controller: devuelve vistas (HTML, Thymeleaf...). Para apps web clásicas.
 * @RestController: devuelve datos (JSON/XML). Es @Controller + @ResponseBody.
 *   @ResponseBody le dice a Spring "serializa el objeto Java a JSON
 *   automáticamente usando Jackson (librería incluida en spring-boot-starter-web)".
 *
 * Para una API REST siempre usas @RestController.
 *
 * @RequestMapping("/api/series"): prefijo de URL para todos los endpoints
 * de este controller. Todos empezarán con /api/series.
 *
 * El CORS (qué orígenes pueden llamar a esta API) ya no se declara aquí por
 * controller — está centralizado en config/SecurityConfig.java, configurable
 * vía la variable de entorno APP_CORS_ALLOWED_ORIGINS.
 */
@RestController
@RequestMapping("/api/series")
@RequiredArgsConstructor
public class SerieController {

    private final SerieService serieService;

    /*
     * GET /api/series  →  lista todas las series
     * GET /api/series?estado=PENDIENTE  →  filtra por estado
     *
     * @RequestParam(required = false): el parámetro de query string es opcional.
     * Si no viene, estado es null y devolvemos todas.
     *
     * ResponseEntity<T>: permite controlar explícitamente el código HTTP
     * de la respuesta además del body. ResponseEntity.ok() → 200 OK.
     */
    @GetMapping
    public ResponseEntity<List<Serie>> listar(
            @RequestParam(required = false) Serie.EstadoSerie estado) {

        List<Serie> series = (estado != null)
                ? serieService.obtenerPorEstado(estado)
                : serieService.obtenerTodas();

        return ResponseEntity.ok(series);
    }

    /*
     * POST /api/series  →  crear nueva serie
     *
     * @RequestBody: deserializa el JSON del body de la petición a un objeto Serie.
     *   Jackson lee el JSON {"titulo": "Breaking Bad"} y rellena los campos.
     *
     * HttpStatus.CREATED → código 201 (el estándar REST para creación exitosa).
     * Usamos ResponseEntity.status(201).body(...) para devolverlo.
     */
    @PostMapping
    public ResponseEntity<Serie> crear(@RequestBody Serie serie) {
        Serie creada = serieService.crear(serie);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    /*
     * PUT /api/series/{id}/vista  →  marcar una serie como vista con nota
     *
     * @PathVariable: extrae el {id} de la URL.
     *   URL: PUT /api/series/5/vista → id = 5
     *
     * Body esperado: { "nota": 8 }
     * Usamos Map<String, Integer> para no crear un DTO solo por un campo.
     * (En proyectos más grandes sí crearíamos una clase MarcarVistaRequest)
     *
     * PUT es idempotente: si lo llamas dos veces con la misma nota, el resultado
     * es el mismo. Por eso es PUT y no POST.
     */
    @PutMapping("/{id}/vista")
    public ResponseEntity<Void> marcarComoVista(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {

        Integer nota = body.get("nota");
        serieService.marcarComoVista(id, nota);
        // 204 No Content: éxito pero no hay nada que devolver en el body
        return ResponseEntity.noContent().build();
    }

    /*
     * DELETE /api/series/{id}  →  eliminar una serie
     *
     * DELETE es idempotente: eliminar algo que no existe tiene el mismo
     * resultado visible que eliminarlo (no está). Aunque aquí devolvemos
     * 404 si no existe — es una decisión de diseño válida y discutible.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Serie> editar(@PathVariable Long id, @RequestBody Serie serie) {
        Serie actualizada = serieService.editar(id, serie);
        return ResponseEntity.ok(actualizada);
    }

    @PutMapping("/{id}/pendiente")
    public ResponseEntity<Void> marcarComoPendiente(@PathVariable Long id) {
        serieService.marcarComoPendiente(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/proceso")
    public ResponseEntity<Void> marcarComoEnProceso(@PathVariable Long id) {
        serieService.marcarComoEnProceso(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/progreso")
    public ResponseEntity<Void> actualizarProgreso(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        serieService.actualizarProgreso(id, body.get("temporada"), body.get("episodio"));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        serieService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/visibilidad")
    public ResponseEntity<Void> alternarOcultoParaAmigos(@PathVariable Long id) {
        serieService.alternarOcultoParaAmigos(id);
        return ResponseEntity.noContent().build();
    }

    /*
     * Manejador global de errores de ESTE controller.
     * Cuando el Service lanza IllegalArgumentException, Spring la captura aquí
     * y devuelve 400 Bad Request con el mensaje de error en JSON.
     *
     * En proyectos grandes se usa @ControllerAdvice en una clase separada
     * para manejar errores de todos los controllers a la vez.
     *
     * @ExceptionHandler: mapea un tipo de excepción a una respuesta HTTP.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}

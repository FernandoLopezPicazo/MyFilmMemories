package com.fernando.seriestracker.controller;

import com.fernando.seriestracker.entity.Pelicula;
import com.fernando.seriestracker.service.PeliculaService;
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
 * @RequestMapping("/api/peliculas"): prefijo de URL para todos los endpoints
 * de este controller. Todos empezarán con /api/peliculas.
 *
 * @CrossOrigin: permite peticiones desde otros orígenes (el frontend Angular
 * en localhost:4200 no puede llamar al backend en localhost:8080 sin esto,
 * por la política CORS del navegador).
 */
@RestController
@RequestMapping("/api/peliculas")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class PeliculaController {

    private final PeliculaService peliculaService;

    /*
     * GET /api/peliculas  →  lista todas las películas
     * GET /api/peliculas?estado=PENDIENTE  →  filtra por estado
     *
     * @RequestParam(required = false): el parámetro de query string es opcional.
     * Si no viene, estado es null y devolvemos todas.
     *
     * ResponseEntity<T>: permite controlar explícitamente el código HTTP
     * de la respuesta además del body. ResponseEntity.ok() → 200 OK.
     */
    @GetMapping
    public ResponseEntity<List<Pelicula>> listar(
            @RequestParam(required = false) Pelicula.EstadoPelicula estado) {

        List<Pelicula> peliculas = (estado != null)
                ? peliculaService.obtenerPorEstado(estado)
                : peliculaService.obtenerTodas();

        return ResponseEntity.ok(peliculas);
    }

    /*
     * POST /api/peliculas  →  crear nueva película
     *
     * @RequestBody: deserializa el JSON del body de la petición a un objeto Pelicula.
     *   Jackson lee el JSON {"titulo": "Breaking Bad"} y rellena los campos.
     *
     * HttpStatus.CREATED → código 201 (el estándar REST para creación exitosa).
     * Usamos ResponseEntity.status(201).body(...) para devolverlo.
     */
    @PostMapping
    public ResponseEntity<Pelicula> crear(@RequestBody Pelicula pelicula) {
        Pelicula creada = peliculaService.crear(pelicula);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    /*
     * PUT /api/peliculas/{id}/vista  →  marcar una película como vista con nota
     *
     * @PathVariable: extrae el {id} de la URL.
     *   URL: PUT /api/peliculas/5/vista → id = 5
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
        peliculaService.marcarComoVista(id, nota);
        // 204 No Content: éxito pero no hay nada que devolver en el body
        return ResponseEntity.noContent().build();
    }

    /*
     * DELETE /api/peliculas/{id}  →  eliminar una película
     *
     * DELETE es idempotente: eliminar algo que no existe tiene el mismo
     * resultado visible que eliminarlo (no está). Aunque aquí devolvemos
     * 404 si no existe — es una decisión de diseño válida y discutible.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Pelicula> editar(@PathVariable Long id, @RequestBody Pelicula pelicula) {
        Pelicula actualizada = peliculaService.editar(id, pelicula);
        return ResponseEntity.ok(actualizada);
    }

    @PutMapping("/{id}/pendiente")
    public ResponseEntity<Void> marcarComoPendiente(@PathVariable Long id) {
        peliculaService.marcarComoPendiente(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        peliculaService.eliminar(id);
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

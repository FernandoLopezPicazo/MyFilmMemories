package com.fernando.seriestracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * @SpringBootApplication es una anotación "compuesta": combina tres en una:
 *   - @Configuration: esta clase puede definir beans de Spring.
 *   - @EnableAutoConfiguration: Spring configura automáticamente todo lo
 *     que detecta en el classpath (JPA si ve Hibernate, web si ve Spring MVC...).
 *   - @ComponentScan: escanea este paquete y subpaquetes buscando clases
 *     anotadas con @Component, @Service, @Repository, @Controller, etc.
 *     y las registra como beans gestionados por Spring.
 *
 * En la entrevista pueden preguntarte: "¿Qué hace @SpringBootApplication?"
 * Respuesta: activa el escaneo de componentes, la autoconfiguración y
 * marca la clase como fuente de configuración, todo en una sola anotación.
 */
@SpringBootApplication
public class SeriesTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeriesTrackerApplication.class, args);
    }
}

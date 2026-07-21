package com.fernando.seriestracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/*
 * El instalador Electron (y el desarrollo local con H2) no tiene login ni
 * usuarios reales — sigue siendo una app de escritorio de un único dueño,
 * tal como funcionaba antes de añadir Supabase Auth. Con spring-boot-starter-
 * security en el classpath, Spring Security bloquearía todo por defecto (auth
 * básica con contraseña generada) si no se declara explícitamente un
 * SecurityFilterChain permisivo para este profile — de eso se encarga esta
 * clase, dejando el comportamiento idéntico al de antes de esta migración.
 */
@Configuration
@EnableWebSecurity
@Profile("!prod")
public class SecurityConfigDev {

    @Bean
    public SecurityFilterChain securityFilterChainDev(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }
}

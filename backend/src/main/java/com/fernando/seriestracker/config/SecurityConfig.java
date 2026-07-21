package com.fernando.seriestracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/*
 * CONCEPTO CLAVE — Resource Server de OAuth2/JWT:
 *
 * Esta app NO emite tokens (eso lo hace Supabase Auth cuando el usuario hace
 * login/registro desde Angular). Spring Boot actúa como "resource server":
 * recibe el JWT que Supabase ya emitió, en la cabecera
 * "Authorization: Bearer <token>", y solo lo VALIDA (firma, expiración,
 * emisor) para saber quién hace la petición.
 *
 * Solo se activa en el profile "prod" (la nube). En "dev" — el instalador
 * Electron local con H2, sin login — se usa SecurityConfigDev en su lugar,
 * que permite todo sin autenticación (ver esa clase para el porqué).
 */
@Configuration
@EnableWebSecurity
@Profile("prod")
public class SecurityConfig {

    @Value("${app.supabase.jwks-uri}")
    private String jwksUri;

    @Value("${app.supabase.issuer}")
    private String issuer;

    @Bean
    public JwtDecoder jwtDecoder() {
        // Supabase firma con ES256 (clave asimétrica ECC P-256) — hay que
        // indicarlo explícitamente, NimbusJwtDecoder asume RS256 por defecto
        // y rechaza cualquier otro algoritmo con "no matching key(s) found".
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwksUri)
                .jwsAlgorithm(SignatureAlgorithm.ES256)
                .build();

        OAuth2TokenValidator<Jwt> validadores = new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                new JwtIssuerValidator(issuer),
                audienceValidator()
        );
        decoder.setJwtValidator(validadores);
        return decoder;
    }

    // Supabase emite JWTs con "aud": "authenticated" para usuarios logueados
    private OAuth2TokenValidator<Jwt> audienceValidator() {
        return token -> {
            if (token.getAudience() != null && token.getAudience().contains("authenticated")) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "El JWT no tiene el audience 'authenticated'", null));
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").authenticated()
                        // El resto (SPA de Angular embebida, /uploads/**) no requiere autenticación
                        .anyRequest().permitAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder())));

        return http.build();
    }
}

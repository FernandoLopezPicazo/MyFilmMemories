package com.fernando.seriestracker.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

// Este parche es específico de H2 (el nombre "CONSTRAINT_9" es autogenerado
// por H2, Postgres nunca lo usaría) — solo aplica al profile "dev".
@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DatabaseMigration {

    private final DataSource dataSource;

    @PostConstruct
    public void dropLegacyConstraints() {
        // Elimina el CHECK constraint que H2 generó automáticamente para el enum
        // estado cuando solo existían PENDIENTE y VISTA. Ahora también existe EN_PROCESO.
        try (Connection conn = dataSource.getConnection()) {
            conn.createStatement().execute("ALTER TABLE SERIES DROP CONSTRAINT IF EXISTS CONSTRAINT_9");
        } catch (SQLException e) {
            // Si no existe o ya fue eliminado, no pasa nada
        }
    }
}

-- Backfill de usuario_id para filas H2 locales creadas antes de que esta
-- columna existiera (instalador Electron / desarrollo local). Spring Boot
-- ejecuta este script ANTES de que Hibernate cree/actualice el esquema
-- (spring.jpa.defer-datasource-initialization=false, el valor por defecto),
-- así que cuando Hibernate intente añadir "usuario_id NOT NULL" ya no hay
-- ninguna fila con el valor a NULL.
--
-- spring.sql.init.mode por defecto es "embedded": este script SOLO se
-- ejecuta contra bases de datos embebidas (H2), nunca contra la Postgres de
-- Supabase en producción — no hace falta acotarlo por profile.
--
-- En una base de datos nueva (sin estas tablas todavía) cada ALTER es un
-- no-op gracias a "IF EXISTS"/"IF NOT EXISTS", y Hibernate crea las tablas
-- normalmente a continuación.

ALTER TABLE IF EXISTS SERIES ADD COLUMN IF NOT EXISTS USUARIO_ID UUID;
UPDATE SERIES SET USUARIO_ID = '00000000-0000-0000-0000-000000000001' WHERE USUARIO_ID IS NULL;

ALTER TABLE IF EXISTS PELICULAS ADD COLUMN IF NOT EXISTS USUARIO_ID UUID;
UPDATE PELICULAS SET USUARIO_ID = '00000000-0000-0000-0000-000000000001' WHERE USUARIO_ID IS NULL;

ALTER TABLE IF EXISTS MANGAS ADD COLUMN IF NOT EXISTS USUARIO_ID UUID;
UPDATE MANGAS SET USUARIO_ID = '00000000-0000-0000-0000-000000000001' WHERE USUARIO_ID IS NULL;

ALTER TABLE IF EXISTS SAGAS ADD COLUMN IF NOT EXISTS USUARIO_ID UUID;
UPDATE SAGAS SET USUARIO_ID = '00000000-0000-0000-0000-000000000001' WHERE USUARIO_ID IS NULL;

-- Mismo patrón para OCULTO_PARA_AMIGOS (Fase 2: amigos y grupos).
ALTER TABLE IF EXISTS SERIES ADD COLUMN IF NOT EXISTS OCULTO_PARA_AMIGOS BOOLEAN;
UPDATE SERIES SET OCULTO_PARA_AMIGOS = FALSE WHERE OCULTO_PARA_AMIGOS IS NULL;

ALTER TABLE IF EXISTS PELICULAS ADD COLUMN IF NOT EXISTS OCULTO_PARA_AMIGOS BOOLEAN;
UPDATE PELICULAS SET OCULTO_PARA_AMIGOS = FALSE WHERE OCULTO_PARA_AMIGOS IS NULL;

ALTER TABLE IF EXISTS MANGAS ADD COLUMN IF NOT EXISTS OCULTO_PARA_AMIGOS BOOLEAN;
UPDATE MANGAS SET OCULTO_PARA_AMIGOS = FALSE WHERE OCULTO_PARA_AMIGOS IS NULL;

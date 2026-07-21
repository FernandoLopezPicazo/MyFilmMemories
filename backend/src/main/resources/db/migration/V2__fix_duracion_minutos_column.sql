-- V1 creó la columna sin comillas como "duracionMinutos", que Postgres pliega
-- a minúsculas ("duracionminutos"). Hibernate, con SpringPhysicalNamingStrategy,
-- espera el nombre físico en snake_case ("duracion_minutos") aunque la entidad
-- declare @Column(name = "duracionMinutos") explícitamente. Nunca se reescribe
-- una migración ya aplicada (V1) — se corrige aquí, en una nueva versión.
alter table peliculas rename column duracionminutos to duracion_minutos;

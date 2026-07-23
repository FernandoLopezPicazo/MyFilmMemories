-- Horario: marcar un titulo como "en emision" con una frecuencia semanal
-- (un dia fijo) o mensual (un dia + que semana del mes), para poder
-- listarlos todos juntos (series/peliculas/mangas) en /api/horario.

alter table series add column en_emision boolean default false;
alter table series add column frecuencia varchar(20);
alter table series add column dia_semana varchar(20);
alter table series add column semana_del_mes integer;

alter table peliculas add column en_emision boolean default false;
alter table peliculas add column frecuencia varchar(20);
alter table peliculas add column dia_semana varchar(20);
alter table peliculas add column semana_del_mes integer;

alter table mangas add column en_emision boolean default false;
alter table mangas add column frecuencia varchar(20);
alter table mangas add column dia_semana varchar(20);
alter table mangas add column semana_del_mes integer;

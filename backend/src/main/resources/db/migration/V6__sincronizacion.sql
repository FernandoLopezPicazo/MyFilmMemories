-- Sincronizacion escritorio<->nube: identificador estable + fecha de
-- ultima modificacion, para poder fusionar el mismo titulo creado en dos
-- bases de datos independientes (columnas nullable: las filas ya existentes
-- se emparejan por titulo la primera vez que se sincronizan).

alter table series add column sync_id uuid;
alter table series add column actualizado_en timestamp;

alter table peliculas add column sync_id uuid;
alter table peliculas add column actualizado_en timestamp;

alter table mangas add column sync_id uuid;
alter table mangas add column actualizado_en timestamp;

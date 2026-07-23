-- Orden manual de peliculas/items dentro de una saga (arrastrar para
-- reordenar, independiente del orden en que se anadieron).

alter table peliculas add column orden integer;
alter table grupo_items add column orden integer;

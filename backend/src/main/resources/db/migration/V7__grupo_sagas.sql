-- Sagas dentro de grupos: agrupar peliculas del grupo en sagas colapsables,
-- igual que las sagas personales. A diferencia del modelo personal, el
-- "visto" de una pelicula de saga es compartido por todo el grupo (no por
-- miembro), asi que vive directamente en grupo_items.estado.

create sequence grupo_saga_seq start with 1 increment by 1;

create table grupo_sagas (
    id bigint primary key default nextval('grupo_saga_seq'),
    grupo_id bigint not null references grupos (id) on delete cascade,
    titulo varchar(255) not null,
    estado varchar(20) not null default 'EN_PROCESO'
);
create index idx_grupo_sagas_grupo_id on grupo_sagas (grupo_id);

alter table grupo_items add column saga_id bigint;
alter table grupo_items add column estado varchar(20) not null default 'PENDIENTE';

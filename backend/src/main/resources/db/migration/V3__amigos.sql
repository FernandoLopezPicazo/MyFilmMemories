-- Fase 2: sistema de amigos (perfiles + solicitudes de amistad + visibilidad)

create table perfiles (
    usuario_id uuid primary key,
    email varchar(255) not null unique
);

create sequence amistad_seq start with 1 increment by 1;

create table amistades (
    id bigint primary key default nextval('amistad_seq'),
    de_usuario_id uuid not null,
    a_usuario_id uuid not null,
    estado varchar(20) not null,
    fecha_solicitud timestamp not null,
    fecha_respuesta timestamp
);
create index idx_amistades_de_usuario_id on amistades (de_usuario_id);
create index idx_amistades_a_usuario_id on amistades (a_usuario_id);

alter table series add column oculto_para_amigos boolean not null default false;
alter table peliculas add column oculto_para_amigos boolean not null default false;
alter table mangas add column oculto_para_amigos boolean not null default false;

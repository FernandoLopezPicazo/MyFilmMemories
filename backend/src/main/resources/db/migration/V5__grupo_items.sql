-- Fase 2: contenido de la lista compartida de un grupo (series/pelis/mangas)
-- y las opiniones de cada miembro sobre cada título.

create sequence grupo_item_seq start with 1 increment by 1;

create table grupo_items (
    id bigint primary key default nextval('grupo_item_seq'),
    grupo_id bigint not null references grupos (id) on delete cascade,
    tipo varchar(20) not null,
    titulo varchar(255) not null,
    descripcion varchar(5000),
    imagen_url varchar(255),
    creado_por uuid not null,
    fecha_creacion timestamp not null
);
create index idx_grupo_items_grupo_id_tipo on grupo_items (grupo_id, tipo);

create table grupo_item_generos (
    grupo_item_id bigint not null references grupo_items (id) on delete cascade,
    genero varchar(255)
);
create index idx_grupo_item_generos_grupo_item_id on grupo_item_generos (grupo_item_id);

create sequence grupo_item_opinion_seq start with 1 increment by 1;

create table grupo_item_opiniones (
    id bigint primary key default nextval('grupo_item_opinion_seq'),
    grupo_item_id bigint not null references grupo_items (id) on delete cascade,
    usuario_id uuid not null,
    nota integer,
    personaje_favorito varchar(255),
    personaje_odiado varchar(255),
    comentario varchar(2000)
);
create unique index idx_grupo_item_opiniones_item_usuario on grupo_item_opiniones (grupo_item_id, usuario_id);

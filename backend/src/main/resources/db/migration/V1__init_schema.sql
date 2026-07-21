-- Esquema inicial para Postgres (Supabase), reflejando las entidades JPA
-- existentes (Serie, Pelicula, Manga, Saga) con la columna usuario_id ya
-- incluida desde el principio: la base de datos nace vacía, así que no hace
-- falta el paso intermedio "nullable -> backfill -> NOT NULL".

create sequence serie_seq start with 1 increment by 1;
create sequence pelicula_seq start with 1 increment by 1;
create sequence manga_seq start with 1 increment by 1;
create sequence saga_seq start with 1 increment by 1;

create table series (
    id bigint primary key default nextval('serie_seq'),
    usuario_id uuid not null,
    titulo varchar(255) not null,
    descripcion varchar(5000),
    imagen_url varchar(255),
    nombre_persona1 varchar(255),
    personaje_favorito varchar(255),
    personaje_odiado varchar(255),
    nota integer,
    nombre_persona2 varchar(255),
    personaje_favorito2 varchar(255),
    personaje_odiado2 varchar(255),
    nota2 integer,
    estado varchar(20) not null,
    fecha_vista date,
    temporada_actual integer,
    episodio_actual integer
);
create index idx_series_usuario_id on series (usuario_id);

create table serie_generos (
    serie_id bigint not null references series (id) on delete cascade,
    genero varchar(255)
);
create index idx_serie_generos_serie_id on serie_generos (serie_id);

create table peliculas (
    id bigint primary key default nextval('pelicula_seq'),
    usuario_id uuid not null,
    titulo varchar(255) not null,
    descripcion varchar(5000),
    imagen_url varchar(255),
    nombre_persona1 varchar(255),
    personaje_favorito varchar(255),
    personaje_odiado varchar(255),
    nota integer,
    nombre_persona2 varchar(255),
    personaje_favorito2 varchar(255),
    personaje_odiado2 varchar(255),
    nota2 integer,
    estado varchar(20) not null,
    fecha_vista date,
    duracionMinutos integer,
    saga_id bigint
);
create index idx_peliculas_usuario_id on peliculas (usuario_id);
create index idx_peliculas_saga_id on peliculas (saga_id);

create table pelicula_generos (
    pelicula_id bigint not null references peliculas (id) on delete cascade,
    genero varchar(255)
);
create index idx_pelicula_generos_pelicula_id on pelicula_generos (pelicula_id);

create table mangas (
    id bigint primary key default nextval('manga_seq'),
    usuario_id uuid not null,
    titulo varchar(255) not null,
    descripcion varchar(5000),
    imagen_url varchar(255),
    capitulo_actual integer,
    url_lectura varchar(500),
    nombre_persona1 varchar(255),
    personaje_favorito varchar(255),
    personaje_odiado varchar(255),
    nota integer,
    nombre_persona2 varchar(255),
    personaje_favorito2 varchar(255),
    personaje_odiado2 varchar(255),
    nota2 integer,
    estado varchar(20) not null,
    fecha_finalizado date
);
create index idx_mangas_usuario_id on mangas (usuario_id);

create table manga_generos (
    manga_id bigint not null references mangas (id) on delete cascade,
    genero varchar(255)
);
create index idx_manga_generos_manga_id on manga_generos (manga_id);

create table sagas (
    id bigint primary key default nextval('saga_seq'),
    usuario_id uuid not null,
    titulo varchar(255) not null,
    estado varchar(20) not null
);
create index idx_sagas_usuario_id on sagas (usuario_id);

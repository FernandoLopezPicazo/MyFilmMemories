-- Fase 2: grupos de amigos (creación, membresía, invitaciones)

create sequence grupo_seq start with 1 increment by 1;

create table grupos (
    id bigint primary key default nextval('grupo_seq'),
    nombre varchar(255) not null,
    creado_por uuid not null,
    fecha_creacion timestamp not null
);

create sequence grupo_miembro_seq start with 1 increment by 1;

create table grupo_miembros (
    id bigint primary key default nextval('grupo_miembro_seq'),
    grupo_id bigint not null references grupos (id) on delete cascade,
    usuario_id uuid not null,
    fecha_union timestamp not null
);
create index idx_grupo_miembros_grupo_id on grupo_miembros (grupo_id);
create index idx_grupo_miembros_usuario_id on grupo_miembros (usuario_id);

create sequence grupo_invitacion_seq start with 1 increment by 1;

create table grupo_invitaciones (
    id bigint primary key default nextval('grupo_invitacion_seq'),
    grupo_id bigint not null references grupos (id) on delete cascade,
    de_usuario_id uuid not null,
    a_usuario_id uuid not null,
    estado varchar(20) not null,
    fecha_invitacion timestamp not null
);
create index idx_grupo_invitaciones_a_usuario_id on grupo_invitaciones (a_usuario_id);

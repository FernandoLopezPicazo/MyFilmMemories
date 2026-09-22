-- El Advisor de seguridad de Supabase marca como CRÍTICO que las 16 tablas
-- de la app (más flyway_schema_history, que crea Flyway) tengan RLS
-- (Row Level Security) desactivado en el esquema "public": Supabase expone
-- automáticamente una API REST (PostgREST) sobre CUALQUIER tabla de ese
-- esquema, accesible con la clave "anon" — que está embebida en el
-- frontend, es pública por diseño. Sin RLS, cualquiera con esa clave puede
-- leer y escribir estas tablas directamente por esa API, saltándose por
-- completo el backend de Spring Boot y sus comprobaciones de propiedad
-- (findByIdAndUsuarioId, comprobarMiembro...).
--
-- La app nunca ha usado esa API REST de Supabase para datos: el backend se
-- conecta a Postgres directamente (SUPABASE_DB_URL/USER/PASSWORD, ver
-- application-prod.properties) como dueño de las tablas, así que activar
-- RLS sin ninguna política ("default deny") bloquea PostgREST por completo
-- sin afectar en nada al backend: en Postgres el dueño de una tabla (y los
-- superusuarios) se salta RLS automáticamente salvo que se use FORCE ROW
-- LEVEL SECURITY, que no usamos aquí.

alter table series enable row level security;
alter table serie_generos enable row level security;
alter table peliculas enable row level security;
alter table pelicula_generos enable row level security;
alter table sagas enable row level security;
alter table mangas enable row level security;
alter table manga_generos enable row level security;
alter table perfiles enable row level security;
alter table amistades enable row level security;
alter table grupos enable row level security;
alter table grupo_miembros enable row level security;
alter table grupo_invitaciones enable row level security;
alter table grupo_items enable row level security;
alter table grupo_item_generos enable row level security;
alter table grupo_item_opiniones enable row level security;
alter table grupo_sagas enable row level security;

-- Tabla interna de Flyway (registro de migraciones aplicadas): no contiene
-- datos de usuario, pero el Advisor la marca igual por estar en "public".
alter table flyway_schema_history enable row level security;

export const environment = {
  production: false,
  tmdbApiKey: 'ed0f419cfd1df6592a4a358a6c7f8e77',
  tmdbBaseUrl: 'https://api.themoviedb.org/3',
  tmdbImageUrl: 'https://image.tmdb.org/t/p/w300',

  // Backend local (dev): Spring Boot con H2, sin login (ver SecurityConfigDev)
  apiUrl: 'http://localhost:8090',

  // Supabase: solo se usa si accedes a esta build con el backend en modo
  // "prod" (nube). En dev/local estas claves no se usan para nada.
  supabaseUrl: '',
  supabaseAnonKey: '',

  // Si false, AuthGuard deja pasar todas las rutas sin sesión — el login
  // (si está configurado) queda como algo opcional, no obligatorio.
  requiereLogin: false,

  // URL del backend en la nube, para la sincronización escritorio↔nube.
  // Sin uso aquí (no hay Supabase configurado en dev puro).
  nubeApiUrl: '',
};

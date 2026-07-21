export const environment = {
  production: true,
  tmdbApiKey: 'ed0f419cfd1df6592a4a358a6c7f8e77',
  tmdbBaseUrl: 'https://api.themoviedb.org/3',
  tmdbImageUrl: 'https://image.tmdb.org/t/p/w300',

  // El instalador de escritorio habla con su propio backend local (H2, sin
  // login exigido — ver SecurityConfigDev/UsuarioActualServiceDev).
  apiUrl: 'http://localhost:8090',

  // Mismas claves públicas que la web (proyecto de Supabase eu-west-1) —
  // necesarias para el login OPCIONAL de "Sincronizar con la nube". Tenerlas
  // configuradas no exige login por sí solo: eso lo decide requiereLogin.
  supabaseUrl: 'https://olacmlyndgtetyhamgod.supabase.co',
  supabaseAnonKey: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9sYWNtbHluZGd0ZXR5aGFtZ29kIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODQ2NDIxOTAsImV4cCI6MjEwMDIxODE5MH0.7HwYO1bcu9J_fFbsc9Evc6ldnUVyzt5fvOfSI9N5eQg',

  // El escritorio nunca exige login — sincronizar con la nube es opcional.
  requiereLogin: false,

  // Backend de la nube contra el que se sincroniza (distinto de apiUrl, que
  // aquí siempre es el backend local).
  nubeApiUrl: 'https://myfilmmemories-backend.onrender.com',
};

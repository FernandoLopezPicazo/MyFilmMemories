# MyFilmMemories 🎬 — Tu catálogo personal de películas, series y mangas

Aplicación **de escritorio para Windows** que te permite llevar un registro de las
películas, series y mangas que has visto: puntuaciones, notas, portadas y estadísticas,
con búsqueda y descubrimiento integrados vía las APIs de **TMDB** y **Jikan (MyAnimeList)**.

Es una aplicación full-stack empaquetada como app de escritorio: frontend **Angular**,
backend **Spring Boot** con base de datos **H2** embebida, y un launcher **Electron**
que arranca todo con doble clic (incluye JRE embebido — no requiere tener Java instalado).

> ⚖️ Código publicado solo como portfolio. Ver [LICENSE.txt](LICENSE.txt).

## ✨ Funcionalidades

- **Catálogo personal** de películas, series y mangas con fichas, portadas y estado de visionado.
- **Explorar**: tendencias, búsqueda y descubrimiento de películas y series (TMDB) y anime/manga (Jikan).
- **Copias de seguridad**: exporta e importa tu colección completa en JSON.
- **Temas visuales** configurables.
- **Datos 100% locales**: base de datos H2 en fichero, sin cuentas ni servidores externos.
- **Instalador para Windows** generado con electron-builder.

## 🛠️ Tecnologías

| Capa | Tecnología |
|---|---|
| Frontend | Angular 16, TypeScript, RxJS |
| Backend | Java 17, Spring Boot 3.2, Spring Data JPA, H2 |
| Escritorio | Electron 33, electron-builder (instalador NSIS con JRE embebido) |
| APIs externas | TMDB (películas/series), Jikan (anime/manga) |

## 🏗️ Arquitectura

```
launcher (Electron)  ──arranca──▶  backend (Spring Boot :8090)  ──sirve──▶  frontend (Angular compilado)
                                        │
                                        └──▶ H2 (fichero local ./data)
```

## 🚀 Cómo ejecutarlo en desarrollo

1. **Clave de TMDB** (gratuita): créala en [themoviedb.org](https://www.themoviedb.org/settings/api)
   y copia `frontend/src/environments/environment.example.ts` como `environment.ts`
   poniendo tu clave (no se sube a git).
2. **Backend**: `cd backend && mvn spring-boot:run` (requiere Java 17 y Maven) — API en `http://localhost:8090`.
3. **Frontend**: `cd frontend && npm install && npm start` — UI en `http://localhost:4200`.

Para generar el instalador de Windows: compila el frontend, empaqueta el backend
(`mvnw package`) y ejecuta `build-installer.ps1` (usa electron-builder).

## 👤 Autor

**Fernando López-Picazo Torres** — flopezpicazotorres@gmail.com

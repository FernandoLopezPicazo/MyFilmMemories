# Rediseño visual — guía de continuación

> Estado: **base hecha** (sistema de diseño + navegación + página Series como
> referencia). Falta migrar el resto de páginas siguiendo el patrón de Series.
> Este documento es autocontenido: con él y los ficheros que cita se puede
> continuar sin más contexto.

## Objetivo
Interfaz más moderna y cómoda, **sin tocar funcionalidad ni HTML** (salvo que
sea imprescindible). Todo el cambio es CSS. Debe verse bien en escritorio y
en móvil (≤ 768 px), en modo claro y oscuro, y respetar la paleta de columnas
que elige el usuario (`ThemeService`).

## Qué está hecho

| Fichero | Qué contiene |
|---|---|
| `frontend/src/styles.css` | **Sistema de diseño.** Tokens (`--acento`, `--bg-*`, `--text-*`, `--border*`, `--shadow*`, `--radius*`, semánticos `--success/--danger/--warning/--info/--teal` y sus `-soft`), reset, tipografía Inter, inputs, botones (`.btn-primary/.btn-secondary/.btn-success/.btn-danger/.btn-edit/.btn-proceso/.btn-explorar/.btn-filtrar/.btn-cargar-mas/.btn-ghost/.btn-icono`), chips (`.genero-chip`, `.genero-tag/.filtro-chip/.explorar-etiqueta` con `.activo/.activa`), `.badge`, `.card`, `.container`, `.error`, modales (`.modal-overlay`, `.modal`, `.modal-cerrar`, `.explorar-overlay`, `.explorar-cerrar`) y su versión móvil tipo *bottom sheet*. |
| `frontend/src/index.html` | Carga de la fuente Inter (con fallback a fuente del sistema) y `viewport-fit=cover`. |
| `frontend/src/app/app.component.css` | Barra superior translúcida; en móvil los enlaces pasan a **barra inferior fija** y las acciones (estado servidor, usuario) se compactan a iconos. El botón hamburguesa queda oculto (`display:none`), no se ha tocado el TS. |
| `frontend/src/app/series-page/series-page.component.css` | **Implementación de referencia.** Reescrita entera sobre tokens: sin hex, sin bloque `:host-context(html.dark)`. En móvil las tres columnas se deslizan horizontalmente con *scroll-snap* y los modales son *bottom sheets*. |

## Reglas para migrar una página (el patrón de Series)

1. **No cambies el HTML** — los nombres de clase se mantienen; solo se
   reescribe el `.component.css`.
2. **Borra** del CSS de la página todo lo que ya define `styles.css`:
   reset (`* {}`/`body {}`), `.container`, `.card`, `.error`, `button` base,
   `.btn-*`, `.genero-chip`, `.genero-tag`, `.filtro-chip`, `.badge`,
   `.modal-overlay`, `.modal`, `.modal-cerrar`, `.explorar-overlay`,
   `.explorar-cerrar`, `.sin-datos`. Si la página los redefine, gana la
   versión local (encapsulación de Angular) y el rediseño no se aplica.
3. **Sustituye cada color fijo por un token**. Equivalencias habituales:
   - `#4f46e5 / #4338ca` → `var(--acento)` · fondo suave → `var(--acento-soft)`
   - `#7c3aed / #5b21b6 / #ede9fe` → `var(--info)` / `var(--info-soft)`
   - `#16a34a / #dcfce7` → `var(--success)` / `var(--success-soft)`
   - `#dc2626 / #fee2e2 / #b91c1c` → `var(--danger)` / `var(--danger-soft)`
   - `#f59e0b` → `var(--warning)` · `#0d9488` → `var(--teal)`
   - `white / #fff` (superficies) → `var(--bg-card)` o `var(--bg-panel)` (modales)
   - `#f8f8ff / #f0f0f0 / #f8fafc` → `var(--bg-card-alt)`
   - `#ddd / #e5e7eb / #e0e0f0 / #eee` → `var(--border)`
   - `#1a1a2e / #333 / #1e293b` → `var(--text-primary)` · `#555 / #444 / #475569` → `var(--text-secondary)` · `#888 / #aaa / #64748b` → `var(--text-muted)`
   - `box-shadow: 0 2px 8px rgba(...)` → `var(--shadow-sm)` / `var(--shadow-md)` / `var(--shadow-lg)`
   - `border-radius: 8/12/16px` → `var(--radius-sm)` / `var(--radius)` / `var(--radius-lg)` / `var(--radius-xl)`
4. **Elimina el bloque `:host-context(html.dark)`** completo: con tokens el
   modo oscuro sale solo. Solo queda si hay algo realmente específico.
5. **Móvil (`@media (max-width: 768px)`)** — copia el bloque de Series:
   barra de acciones en columna, columnas con *scroll-snap* horizontal
   (`.columnas` → `display:flex; overflow-x:auto; scroll-snap-type:x mandatory`,
   `.seccion` → `flex:0 0 88%; scroll-snap-align:center`), grids de personas /
   opiniones a una columna, Explorar a pantalla completa.
6. Compila (`cd frontend && npx ng build --configuration electron`) y revisa
   en el navegador **claro + oscuro + móvil** (DevTools, 390 px de ancho).

## Pendiente, por prioridad

1. `peliculas-page.component.css` (1.100 líneas) — igual que Series pero
   además tiene **sagas** (`.saga-card`, arrastre de películas a sagas):
   respeta las clases de drag & drop (`.drag-sobre`, `.drop-zone`).
2. `mangas-page.component.css` (700 líneas) — tiene progreso por capítulo
   (`.btn-step`, `.cap-valor`, `.btn-abrir-url`); estílalos como
   `.btn-flecha` / `.progreso-valor` de Series.
3. `grupos-page.component.css` (500 líneas) y `amigos-page.component.css`.
4. `login.component.css` — tarjeta centrada con `.card` y `--shadow-lg`.
5. `backup-page.component.css` y `horario-page.component.css` (esta última
   ya usa tokens; solo revisar móvil).
6. Extra opcional: en `shared/icon/icon.component.ts` subir el tamaño de los
   iconos de la barra inferior (hoy `[size]="18"` en `app.component.html`)
   a 22 px en móvil — requiere tocar HTML o usar `transform: scale()` en CSS.

## Cosas que NO hay que hacer
- No renombrar `--acento`, `--color-pendiente`, `--color-proceso`,
  `--color-finalizado`: los escribe `ThemeService` en tiempo de ejecución.
- No usar `!important` para ganar a un estilo local: bórralo del CSS local.
- No introducir librerías de UI ni frameworks CSS: el sistema es propio.
- No cambiar el flujo de las páginas (botones, modales, arrastres): el
  rediseño es solo visual.

## Cómo probar en local
```bash
# backend (H2, sin login)
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
# frontend
cd frontend && npx ng serve --configuration electron --port 4200
```
Abre `http://localhost:4200`, alterna claro/oscuro con el botón de la barra y
usa la vista móvil de DevTools.

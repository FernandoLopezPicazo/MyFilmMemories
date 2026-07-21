import { Component, ElementRef, ViewChild } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Serie } from '../serie.service';
import { Pelicula } from '../pelicula.service';
import { Manga } from '../manga.service';
import { environment } from '../../environments/environment';
import { AuthService } from '../auth.service';
import { SincronizacionService, ResultadoSincronizacion } from '../sincronizacion.service';

interface BackupFile {
  version: number;
  fecha: string;
  series?: Serie[];
  peliculas?: Pelicula[];
  mangas?: Manga[];
}

interface ItemImport<T> {
  item: T;
  seleccionado: boolean;
  duplicado: boolean;
}

@Component({
  selector: 'app-backup-page',
  templateUrl: './backup-page.component.html',
  styleUrls: ['./backup-page.component.css']
})
export class BackupPageComponent {

  @ViewChild('fileInput') fileInputRef!: ElementRef<HTMLInputElement>;

  // ── EXPORTAR ─────────────────────────────────────
  exportSeries    = true;
  exportPeliculas = true;
  exportMangas    = true;

  exportSeriesPendiente   = true;
  exportSeriesProceso     = true;
  exportSeriesVista       = true;
  exportPeliculasPendiente = true;
  exportPeliculasVista    = true;
  exportMangasPendiente   = true;
  exportMangasProceso     = true;
  exportMangasFinalizados = true;

  exportando = false;
  exportError = '';

  // ── IMPORTAR ─────────────────────────────────────
  modoVista: 'export' | 'import' | 'sincronizar' = 'export';
  importando = false;
  importError = '';
  importOk = 0;
  importSaltados = 0;

  archivoNombre = '';
  backupCargado: BackupFile | null = null;

  importSeries:    ItemImport<Serie>[]    = [];
  importPeliculas: ItemImport<Pelicula>[] = [];
  importMangas:    ItemImport<Manga>[]    = [];

  // ── SINCRONIZACIÓN CON LA NUBE (opcional, solo si Supabase está
  // configurado en este build — ver environment.electron.ts) ─────
  emailLogin = '';
  passwordLogin = '';
  loginCargando = false;
  loginError = '';

  sincronizando = false;
  sincronizacionError = '';
  ultimoResultadoSincronizacion: ResultadoSincronizacion | null = null;

  constructor(
    private http: HttpClient,
    public auth: AuthService,
    private sincronizacionService: SincronizacionService
  ) {}

  async iniciarSesionSincronizacion(): Promise<void> {
    if (!this.emailLogin.trim() || !this.passwordLogin.trim()) return;
    this.loginError = '';
    this.loginCargando = true;
    try {
      await this.auth.signIn(this.emailLogin.trim(), this.passwordLogin);
      this.passwordLogin = '';
    } catch (e: any) {
      this.loginError = e?.message || 'No se pudo iniciar sesión';
    } finally {
      this.loginCargando = false;
    }
  }

  async sincronizarAhora(): Promise<void> {
    this.sincronizacionError = '';
    this.sincronizando = true;
    this.ultimoResultadoSincronizacion = null;
    try {
      this.ultimoResultadoSincronizacion = await this.sincronizacionService.sincronizar();
    } catch (e: any) {
      this.sincronizacionError = e?.error?.error || e?.message || 'Error durante la sincronización';
    } finally {
      this.sincronizando = false;
    }
  }

  async abrirSelectorArchivo(): Promise<void> {
    // API moderna (Chrome/Edge): abre el diálogo directamente en la carpeta Descargas
    const w = window as any;
    if (typeof w.showOpenFilePicker === 'function') {
      try {
        const [handle] = await w.showOpenFilePicker({
          startIn: 'downloads',
          multiple: false,
          types: [{
            description: 'Archivo JSON de MyFilmMemories',
            accept: { 'application/json': ['.json'] }
          }]
        });
        const file = await handle.getFile();
        this.procesarArchivo(file);
      } catch (e: any) {
        // El usuario canceló el diálogo → no es un error real, lo ignoramos
        if (e && e.name !== 'AbortError') {
          this.importError = 'No se pudo abrir el selector de archivos.';
        }
      }
      return;
    }
    // Fallback para navegadores sin la API moderna: input clásico
    this.fileInputRef.nativeElement.click();
  }

  // ── EXPORTAR ─────────────────────────────────────

  exportar(): void {
    this.exportando = true;
    this.exportError = '';

    const peticiones: any = {};
    if (this.exportSeries) {
      if (this.exportSeriesPendiente) peticiones['seriesPendiente'] =
        this.http.get<Serie[]>(`${environment.apiUrl}/api/series?estado=PENDIENTE`).pipe(catchError(() => of([])));
      if (this.exportSeriesProceso) peticiones['seriesProceso'] =
        this.http.get<Serie[]>(`${environment.apiUrl}/api/series?estado=EN_PROCESO`).pipe(catchError(() => of([])));
      if (this.exportSeriesVista) peticiones['seriesVista'] =
        this.http.get<Serie[]>(`${environment.apiUrl}/api/series?estado=VISTA`).pipe(catchError(() => of([])));
    }
    if (this.exportPeliculas) {
      if (this.exportPeliculasPendiente) peticiones['peliculasPendiente'] =
        this.http.get<Pelicula[]>(`${environment.apiUrl}/api/peliculas?estado=PENDIENTE`).pipe(catchError(() => of([])));
      if (this.exportPeliculasVista) peticiones['peliculasVista'] =
        this.http.get<Pelicula[]>(`${environment.apiUrl}/api/peliculas?estado=VISTA`).pipe(catchError(() => of([])));
    }
    if (this.exportMangas) {
      if (this.exportMangasPendiente) peticiones['mangasPendiente'] =
        this.http.get<Manga[]>(`${environment.apiUrl}/api/mangas?estado=PENDIENTE`).pipe(catchError(() => of([])));
      if (this.exportMangasProceso) peticiones['mangasProceso'] =
        this.http.get<Manga[]>(`${environment.apiUrl}/api/mangas?estado=EN_PROCESO`).pipe(catchError(() => of([])));
      if (this.exportMangasFinalizados) peticiones['mangasFinalizados'] =
        this.http.get<Manga[]>(`${environment.apiUrl}/api/mangas?estado=FINALIZADO`).pipe(catchError(() => of([])));
    }

    if (Object.keys(peticiones).length === 0) {
      this.exportError = 'Selecciona al menos una categoría y estado.';
      this.exportando = false;
      return;
    }

    forkJoin(peticiones).subscribe({
      next: (res: any) => {
        const series: Serie[] = [
          ...(res['seriesPendiente'] || []),
          ...(res['seriesProceso'] || []),
          ...(res['seriesVista'] || []),
        ];
        const peliculas: Pelicula[] = [
          ...(res['peliculasPendiente'] || []),
          ...(res['peliculasVista'] || []),
        ];
        const mangas: Manga[] = [
          ...(res['mangasPendiente'] || []),
          ...(res['mangasProceso'] || []),
          ...(res['mangasFinalizados'] || []),
        ];

        const backup: BackupFile = {
          version: 1,
          fecha: new Date().toISOString(),
          ...(series.length    ? { series }    : {}),
          ...(peliculas.length ? { peliculas } : {}),
          ...(mangas.length    ? { mangas }    : {}),
        };

        const blob = new Blob([JSON.stringify(backup, null, 2)], { type: 'application/json' });
        const url  = URL.createObjectURL(blob);
        const a    = document.createElement('a');
        a.href     = url;
        a.download = `myfilmmemories_${new Date().toISOString().slice(0,10)}.json`;
        a.click();
        URL.revokeObjectURL(url);
        this.exportando = false;
      },
      error: () => {
        this.exportError = 'Error al conectar con el servidor.';
        this.exportando = false;
      }
    });
  }

  // ── IMPORTAR ─────────────────────────────────────

  onArchivoSeleccionado(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    this.procesarArchivo(input.files[0]);
  }

  private procesarArchivo(file: File): void {
    this.archivoNombre = file.name;
    this.importError   = '';
    this.importOk      = 0;
    this.importSaltados = 0;
    this.backupCargado = null;

    const reader = new FileReader();
    reader.onload = (e) => {
      try {
        const json = JSON.parse(e.target?.result as string) as BackupFile;
        this.cargarPreview(json);
      } catch {
        this.importError = 'El archivo no es un JSON válido de MyFilmMemories.';
      }
    };
    reader.readAsText(file);
  }

  private cargarPreview(json: BackupFile): void {
    // Cargar existentes para marcar duplicados
    forkJoin({
      series:    this.http.get<Serie[]>   (`${environment.apiUrl}/api/series`).pipe(catchError(() => of([]))),
      peliculas: this.http.get<Pelicula[]>(`${environment.apiUrl}/api/peliculas`).pipe(catchError(() => of([]))),
      mangas:    this.http.get<Manga[]>   (`${environment.apiUrl}/api/mangas`).pipe(catchError(() => of([]))),
    }).subscribe(existentes => {
      const titulosSeries    = new Set(existentes.series.map(s => s.titulo.toLowerCase()));
      const titulosPeliculas = new Set(existentes.peliculas.map(p => p.titulo.toLowerCase()));
      const titulosMangas    = new Set(existentes.mangas.map(m => m.titulo.toLowerCase()));

      this.importSeries    = (json.series    || []).map(s => ({
        item: s, seleccionado: true, duplicado: titulosSeries.has(s.titulo.toLowerCase())
      }));
      this.importPeliculas = (json.peliculas || []).map(p => ({
        item: p, seleccionado: true, duplicado: titulosPeliculas.has(p.titulo.toLowerCase())
      }));
      this.importMangas    = (json.mangas    || []).map(m => ({
        item: m, seleccionado: true, duplicado: titulosMangas.has(m.titulo.toLowerCase())
      }));

      // Pre-deseleccionar duplicados
      this.importSeries.forEach(x    => { if (x.duplicado) x.seleccionado = false; });
      this.importPeliculas.forEach(x => { if (x.duplicado) x.seleccionado = false; });
      this.importMangas.forEach(x    => { if (x.duplicado) x.seleccionado = false; });

      this.backupCargado = json;
    });
  }

  totalSeleccionados(): number {
    return this.importSeries.filter(x => x.seleccionado).length
      + this.importPeliculas.filter(x => x.seleccionado).length
      + this.importMangas.filter(x => x.seleccionado).length;
  }

  importar(): void {
    if (this.totalSeleccionados() === 0) return;
    this.importando   = true;
    this.importError  = '';
    this.importOk     = 0;
    this.importSaltados = 0;

    const series    = this.importSeries.filter(x => x.seleccionado).map(x => ({ ...x.item, id: undefined }));
    const peliculas = this.importPeliculas.filter(x => x.seleccionado).map(x => ({ ...x.item, id: undefined }));
    const mangas    = this.importMangas.filter(x => x.seleccionado).map(x => ({ ...x.item, id: undefined }));

    const saltados = this.importSeries.filter(x => !x.seleccionado).length
      + this.importPeliculas.filter(x => !x.seleccionado).length
      + this.importMangas.filter(x => !x.seleccionado).length;

    const peticiones: any[] = [
      ...series.map(s    => this.http.post(`${environment.apiUrl}/api/series`, s).pipe(catchError(() => of(null)))),
      ...peliculas.map(p => this.http.post(`${environment.apiUrl}/api/peliculas`, p).pipe(catchError(() => of(null)))),
      ...mangas.map(m    => this.http.post(`${environment.apiUrl}/api/mangas`, m).pipe(catchError(() => of(null)))),
    ];

    if (peticiones.length === 0) { this.importando = false; return; }

    forkJoin(peticiones).subscribe({
      next: (resultados: any[]) => {
        this.importOk      = resultados.filter(r => r !== null).length;
        this.importSaltados = saltados;
        this.importando    = false;
        // Limpiar preview
        this.importSeries    = [];
        this.importPeliculas = [];
        this.importMangas    = [];
        this.backupCargado   = null;
        this.archivoNombre   = '';
      },
      error: () => {
        this.importError = 'Error durante la importación.';
        this.importando  = false;
      }
    });
  }

  estadoLabel(estado: string): string {
    const map: Record<string, string> = {
      PENDIENTE: 'Pendiente', EN_PROCESO: 'En proceso',
      VISTA: 'Vista', FINALIZADO: 'Finalizado'
    };
    return map[estado] ?? estado;
  }
}

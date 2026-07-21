import { Component, OnInit } from '@angular/core';
import { Manga, MangaService } from '../manga.service';
import { ExplorarService, ResultadoExplorar } from '../explorar.service';

const GENEROS_MANGA = [
  'Acción', 'Artes Marciales', 'Aventura', 'Ciencia Ficción', 'Comedia',
  'Deportes', 'Drama', 'Ecchi', 'Fantasía', 'Harem', 'Horror', 'Isekai',
  'Josei', 'Magia', 'Mecha', 'Misterio', 'Música', 'Psicológico', 'Romance',
  'Seinen', 'Shoujo', 'Shounen', 'Slice of Life', 'Sobrenatural', 'Vampiros'
];

@Component({
  selector: 'app-mangas-page',
  templateUrl: './mangas-page.component.html',
  styleUrls: ['./mangas-page.component.css']
})
export class MangasPageComponent implements OnInit {

  pendientes: Manga[] = [];
  enProceso: Manga[] = [];
  finalizados: Manga[] = [];
  error = '';

  dragSobrePendiente = false;
  dragSobreEnProceso = false;
  dragSobreFinalizado = false;

  // Formulario nuevo manga
  tituloNuevo = '';
  mostrarFormulario = false;

  // Buscador y filtros
  busqueda = '';
  generosSeleccionados: string[] = [];
  mostrarFiltro = false;

  get pendientesFiltrados() { return this.filtrar(this.pendientes); }
  get enProcesoFiltrados()  { return this.filtrar(this.enProceso); }
  get finalizadosFiltrados(){ return this.filtrar(this.finalizados); }

  private filtrar(lista: Manga[]): Manga[] {
    let resultado = lista;
    const q = this.busqueda.trim().toLowerCase();
    if (q) resultado = resultado.filter(m => m.titulo.toLowerCase().includes(q));
    if (this.generosSeleccionados.length > 0) {
      resultado = resultado.filter(m =>
        this.generosSeleccionados.some(g => m.generos?.includes(g))
      );
    }
    return resultado;
  }

  toggleFiltroGenero(genero: string): void {
    const idx = this.generosSeleccionados.indexOf(genero);
    if (idx >= 0) this.generosSeleccionados.splice(idx, 1);
    else this.generosSeleccionados.push(genero);
    this.generosSeleccionados = [...this.generosSeleccionados];
  }

  // Modal detalle
  mangaDetalle: Manga | null = null;

  // Modal finalizar
  mangaFinalizando: Manga | null = null;
  finalizarForm: Partial<Manga> = {};
  imagenFinalizarSeleccionada: File | null = null;
  imagenFinalizarPreview: string | null = null;
  mostrarGenerosFinalizando = false;

  // Modal edición
  mangaEditando: Manga | null = null;
  imagenEditPreview: string | null = null;
  imagenEditSeleccionada: File | null = null;
  mostrarGenerosEdit = false;

  readonly GENEROS = GENEROS_MANGA;

  // ── EXPLORAR ───────────────────────────────────────
  mostrarExplorar = false;
  explorarResultados: ResultadoExplorar[] = [];
  explorarBusqueda = '';
  explorarCargando = false;
  explorarCargandoMas = false;
  explorarPagina = 1;
  explorarGenerosActivos: string[] = [];
  explorarDetalle: ResultadoExplorar | null = null;
  explorarAgregando = false;

  get explorarFiltrados(): ResultadoExplorar[] {
    return this.explorarResultados;
  }

  get explorarGenerosTodos(): string[] {
    return this.GENEROS;
  }

  toggleExplorarGenero(g: string): void {
    const idx = this.explorarGenerosActivos.indexOf(g);
    if (idx >= 0) this.explorarGenerosActivos.splice(idx, 1);
    else this.explorarGenerosActivos.push(g);
    this.explorarGenerosActivos = [...this.explorarGenerosActivos];
    this.buscarPorGeneros();
  }

  buscarPorGeneros(): void {
    if (this.explorarGenerosActivos.length === 0) { this.abrirExplorar(); return; }
    this.explorarPagina = 1;
    this.explorarCargando = true;
    this.explorarService.descubrirMangas(this.explorarGenerosActivos, 1).subscribe({
      next: (r) => { this.explorarResultados = r; this.explorarCargando = false; },
      error: () => { this.explorarCargando = false; }
    });
  }

  abrirExplorar(): void {
    this.mostrarExplorar = true;
    this.explorarBusqueda = '';
    this.explorarGenerosActivos = [];
    this.explorarPagina = 1;
    this.explorarCargando = true;
    this.explorarService.topMangas(1).subscribe({
      next: (r) => { this.explorarResultados = r; this.explorarCargando = false; },
      error: () => { this.explorarCargando = false; }
    });
  }

  buscarExplorar(): void {
    const q = this.explorarBusqueda.trim();
    if (!q) { this.abrirExplorar(); return; }
    this.explorarPagina = 1;
    this.explorarCargando = true;
    this.explorarService.buscarMangas(q, 1).subscribe({
      next: (r) => { this.explorarResultados = r; this.explorarCargando = false; },
      error: () => { this.explorarCargando = false; }
    });
  }

  cargarMasExplorar(): void {
    this.explorarPagina++;
    this.explorarCargandoMas = true;
    const q = this.explorarBusqueda.trim();
    const obs = this.explorarGenerosActivos.length
      ? this.explorarService.descubrirMangas(this.explorarGenerosActivos, this.explorarPagina)
      : q
        ? this.explorarService.buscarMangas(q, this.explorarPagina)
        : this.explorarService.topMangas(this.explorarPagina);
    obs.subscribe({
      next: (r) => { this.explorarResultados = [...this.explorarResultados, ...r]; this.explorarCargandoMas = false; },
      error: () => { this.explorarPagina--; this.explorarCargandoMas = false; }
    });
  }

  abrirDetalleExplorar(r: ResultadoExplorar): void {
    this.explorarDetalle = r;
    this.explorarAgregando = false;
  }

  agregarDesdeExplorar(r: ResultadoExplorar): void {
    this.explorarAgregando = true;
    this.mangaService.crear({
      titulo: r.titulo,
      descripcion: r.descripcion,
      imagenUrl: r.imagenUrl || undefined,
      generos: r.generos,
      estado: 'PENDIENTE'
    }).subscribe({
      next: () => {
        this.explorarDetalle = null;
        this.explorarAgregando = false;
        this.cargarMangas();
      },
      error: () => { this.explorarAgregando = false; }
    });
  }

  constructor(private mangaService: MangaService, private explorarService: ExplorarService) {}

  ngOnInit(): void {
    this.cargarMangas();
  }

  cargarMangas(): void {
    this.mangaService.obtenerPorEstado('PENDIENTE').subscribe({
      next: (data) => this.pendientes = data,
      error: () => this.error = 'No se pudo conectar con el servidor'
    });
    this.mangaService.obtenerPorEstado('EN_PROCESO').subscribe({
      next: (data) => this.enProceso = data
    });
    this.mangaService.obtenerPorEstado('FINALIZADO').subscribe({
      next: (data) => this.finalizados = data
    });
  }

  // ── CREAR ─────────────────────────────────────────
  crear(): void {
    if (!this.tituloNuevo.trim()) return;
    this.mangaService.crear({ titulo: this.tituloNuevo.trim(), estado: 'PENDIENTE' }).subscribe({
      next: () => {
        this.tituloNuevo = '';
        this.mostrarFormulario = false;
        this.cargarMangas();
      }
    });
  }

  // ── DETALLE ───────────────────────────────────────
  abrirDetalle(manga: Manga): void {
    this.mangaDetalle = { ...manga };
  }

  // ── EDICIÓN ───────────────────────────────────────
  abrirEdicion(manga: Manga, event: Event): void {
    event.stopPropagation();
    this.mangaDetalle = null;
    this.mangaEditando = { ...manga, generos: [...(manga.generos || [])] };
    this.imagenEditPreview = null;
    this.imagenEditSeleccionada = null;
    this.mostrarGenerosEdit = false;
  }

  onImagenEditSeleccionada(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    this.imagenEditSeleccionada = input.files[0];
    const reader = new FileReader();
    reader.onload = () => this.imagenEditPreview = reader.result as string;
    reader.readAsDataURL(this.imagenEditSeleccionada);
  }

  guardarEdicion(): void {
    if (!this.mangaEditando?.id) return;
    const guardar = (imagenUrl?: string) => {
      const datos = { ...this.mangaEditando, ...(imagenUrl ? { imagenUrl } : {}) };
      this.mangaService.editar(this.mangaEditando!.id!, datos).subscribe({
        next: () => {
          this.mangaEditando = null;
          this.imagenEditPreview = null;
          this.imagenEditSeleccionada = null;
          this.cargarMangas();
        }
      });
    };
    if (this.imagenEditSeleccionada) {
      this.mangaService.subirImagen(this.imagenEditSeleccionada).subscribe({
        next: (res) => guardar(res.url),
        error: () => guardar()
      });
    } else {
      guardar();
    }
  }

  // ── PROGRESO (EN_PROCESO inline) ──────────────────
  cambiarCapitulo(manga: Manga, delta: number): void {
    const nuevo = Math.max(1, (manga.capituloActual || 1) + delta);
    manga.capituloActual = nuevo;
    this.mangaService.actualizarProgreso(manga.id!, nuevo, manga.urlLectura || '').subscribe();
  }

  guardarUrl(manga: Manga): void {
    this.mangaService.actualizarProgreso(manga.id!, manga.capituloActual || 1, manga.urlLectura || '').subscribe();
  }

  // ── MODAL FINALIZAR ───────────────────────────────
  abrirModalFinalizar(manga: Manga, event?: Event): void {
    event?.stopPropagation();
    this.mangaDetalle = null;
    this.mangaFinalizando = manga;
    this.finalizarForm = {
      descripcion: manga.descripcion || '',
      imagenUrl: manga.imagenUrl,
      generos: [...(manga.generos || [])],
      urlLectura: manga.urlLectura || '',
      nombrePersona1: manga.nombrePersona1 || '',
      personajeFavorito: manga.personajeFavorito || '',
      personajeOdiado: manga.personajeOdiado || '',
      nota: 5,
      nombrePersona2: manga.nombrePersona2 || '',
      personajeFavorito2: manga.personajeFavorito2 || '',
      personajeOdiado2: manga.personajeOdiado2 || '',
      nota2: undefined
    };
    this.imagenFinalizarSeleccionada = null;
    this.imagenFinalizarPreview = null;
    this.mostrarGenerosFinalizando = false;
  }

  onImagenFinalizarSeleccionada(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    this.imagenFinalizarSeleccionada = input.files[0];
    const reader = new FileReader();
    reader.onload = () => this.imagenFinalizarPreview = reader.result as string;
    reader.readAsDataURL(this.imagenFinalizarSeleccionada);
  }

  confirmarFinalizar(): void {
    if (!this.mangaFinalizando?.id) return;
    const id = this.mangaFinalizando.id;

    const finalizar = (imagenUrl?: string) => {
      const datos = { ...this.finalizarForm, ...(imagenUrl ? { imagenUrl } : {}) };
      this.mangaService.finalizar(id, datos).subscribe({
        next: () => {
          this.mangaFinalizando = null;
          this.cargarMangas();
        },
        error: (e) => this.error = e.error?.error || 'Error al finalizar'
      });
    };

    if (this.imagenFinalizarSeleccionada) {
      this.mangaService.subirImagen(this.imagenFinalizarSeleccionada).subscribe({
        next: (res) => finalizar(res.url),
        error: () => finalizar()
      });
    } else {
      finalizar();
    }
  }

  // ── EMPEZAR ───────────────────────────────────────
  empezar(id: number, event: Event): void {
    event.stopPropagation();
    this.mangaService.marcarComoEnProceso(id).subscribe({ next: () => this.cargarMangas() });
  }

  empezarDesdeDetalle(): void {
    if (!this.mangaDetalle?.id) return;
    this.mangaService.marcarComoEnProceso(this.mangaDetalle.id).subscribe({
      next: () => { this.mangaDetalle = null; this.cargarMangas(); }
    });
  }

  // ── ELIMINAR ──────────────────────────────────────
  eliminar(id: number, event: Event): void {
    event.stopPropagation();
    this.mangaService.eliminar(id).subscribe({
      next: () => this.cargarMangas()
    });
  }

  alternarVisibilidad(manga: Manga, event: Event): void {
    event.stopPropagation();
    this.mangaService.alternarVisibilidad(manga.id!).subscribe(() => {
      manga.ocultoParaAmigos = !manga.ocultoParaAmigos;
    });
  }

  // ── DRAG & DROP ───────────────────────────────────
  onDragStart(manga: Manga, event: DragEvent): void {
    event.dataTransfer!.setData('mangaId', manga.id!.toString());
    event.dataTransfer!.setData('estadoOrigen', manga.estado);
    event.dataTransfer!.effectAllowed = 'move';
  }

  onDragOver(event: DragEvent, seccion: string): void {
    event.preventDefault();
    event.dataTransfer!.dropEffect = 'move';
    this.dragSobrePendiente  = seccion === 'PENDIENTE';
    this.dragSobreEnProceso  = seccion === 'EN_PROCESO';
    this.dragSobreFinalizado = seccion === 'FINALIZADO';
  }

  onDragLeave(event: DragEvent, seccion: string): void {
    const zona = event.currentTarget as HTMLElement;
    const dest = event.relatedTarget as Node;
    if (dest && zona.contains(dest)) return;
    if (seccion === 'PENDIENTE')  this.dragSobrePendiente = false;
    if (seccion === 'EN_PROCESO') this.dragSobreEnProceso = false;
    if (seccion === 'FINALIZADO') this.dragSobreFinalizado = false;
  }

  onDrop(event: DragEvent, destino: 'PENDIENTE' | 'EN_PROCESO' | 'FINALIZADO'): void {
    event.preventDefault();
    this.dragSobrePendiente = this.dragSobreEnProceso = this.dragSobreFinalizado = false;
    const id = parseInt(event.dataTransfer!.getData('mangaId'), 10);
    const origen = event.dataTransfer!.getData('estadoOrigen');
    if (!id || origen === destino) return;

    if (destino === 'PENDIENTE') {
      this.mangaService.marcarComoPendiente(id).subscribe({
        next: () => this.cargarMangas(),
        error: (e) => this.error = 'Error: ' + (e.error?.error || e.status)
      });
    } else if (destino === 'EN_PROCESO') {
      this.mangaService.marcarComoEnProceso(id).subscribe({
        next: () => this.cargarMangas(),
        error: (e) => this.error = 'Error: ' + (e.error?.error || e.status)
      });
    } else {
      const todas = [...this.pendientes, ...this.enProceso, ...this.finalizados];
      const manga = todas.find(m => m.id === id);
      if (manga) this.abrirModalFinalizar(manga);
    }
  }

  // ── GÉNEROS ───────────────────────────────────────
  toggleGeneroFinalizar(genero: string): void {
    const arr = this.finalizarForm.generos || [];
    const idx = arr.indexOf(genero);
    if (idx >= 0) arr.splice(idx, 1); else arr.push(genero);
    this.finalizarForm.generos = [...arr];
  }

  toggleGeneroEdicion(genero: string): void {
    if (!this.mangaEditando) return;
    const arr = this.mangaEditando.generos || [];
    const idx = arr.indexOf(genero);
    if (idx >= 0) arr.splice(idx, 1); else arr.push(genero);
    this.mangaEditando.generos = [...arr];
  }

  tieneGenero(lista: string[] | undefined, genero: string): boolean {
    return !!(lista?.includes(genero));
  }

  imagenReal(url: string): string {
    return url.startsWith('http') ? url : 'http://localhost:8090' + url;
  }

  tienePersona2(manga: Manga): boolean {
    return !!(manga.nombrePersona2 || manga.personajeFavorito2 || manga.personajeOdiado2);
  }
}

import { Component, OnInit } from '@angular/core';
import { Serie, SerieService } from '../serie.service';
import { ExplorarService, ResultadoExplorar } from '../explorar.service';

const GENEROS_SERIE = [
  'Acción y aventura','Animación','Ciencia ficción y fantasía','Comedia','Crimen',
  'Documental','Drama','Familia','Guerra y política','Infantil',
  'Misterio','Reality','Suspenso','Terror','Western'
];

@Component({
  selector: 'app-series-page',
  templateUrl: './series-page.component.html',
  styleUrls: ['./series-page.component.css']
})
export class SeriesPageComponent implements OnInit {

  pendientes: Serie[] = [];
  enProceso: Serie[] = [];
  vistas: Serie[] = [];
  dragSobrePendiente = false;
  dragSobreEnProceso = false;
  dragSobreVista = false;
  error = '';

  // Formulario nueva serie (solo título)
  tituloNueva = '';
  mostrarFormulario = false;

  // Buscador y filtros
  busqueda = '';
  generosSeleccionados: string[] = [];
  mostrarFiltro = false;

  get pendientesFiltradas() { return this.filtrar(this.pendientes); }
  get enProcesoFiltradas()  { return this.filtrar(this.enProceso); }
  get vistasFiltradas()     { return this.filtrar(this.vistas); }

  private filtrar(lista: Serie[]): Serie[] {
    let resultado = lista;
    const q = this.busqueda.trim().toLowerCase();
    if (q) resultado = resultado.filter(s => s.titulo.toLowerCase().includes(q));
    if (this.generosSeleccionados.length > 0) {
      resultado = resultado.filter(s =>
        this.generosSeleccionados.some(g => s.generos?.includes(g))
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
  serieDetalle: Serie | null = null;

  // Modal marcar como vista (formulario completo)
  serieSeleccionada: Serie | null = null;
  vistaForm: Partial<Serie> = {};
  imagenVistaSeleccionada: File | null = null;
  imagenVistaPreview: string | null = null;
  mostrarGenerosVista = false;

  // Modal edición
  serieEditando: Serie | null = null;
  imagenEditPreview: string | null = null;
  imagenEditSeleccionada: File | null = null;
  mostrarGenerosEdit = false;

  readonly GENEROS = GENEROS_SERIE;

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
    this.explorarService.descubrirSeries(this.explorarGenerosActivos, 1).subscribe({
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
    this.explorarService.trendingSeries(1).subscribe({
      next: (r) => { this.explorarResultados = r; this.explorarCargando = false; },
      error: () => { this.explorarCargando = false; }
    });
  }

  buscarExplorar(): void {
    const q = this.explorarBusqueda.trim();
    if (!q) { this.abrirExplorar(); return; }
    this.explorarPagina = 1;
    this.explorarCargando = true;
    this.explorarService.buscarSeries(q, 1).subscribe({
      next: (r) => { this.explorarResultados = r; this.explorarCargando = false; },
      error: () => { this.explorarCargando = false; }
    });
  }

  cargarMasExplorar(): void {
    this.explorarPagina++;
    this.explorarCargandoMas = true;
    const q = this.explorarBusqueda.trim();
    const obs = this.explorarGenerosActivos.length
      ? this.explorarService.descubrirSeries(this.explorarGenerosActivos, this.explorarPagina)
      : q
        ? this.explorarService.buscarSeries(q, this.explorarPagina)
        : this.explorarService.trendingSeries(this.explorarPagina);
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
    this.serieService.crear({
      titulo: r.titulo,
      descripcion: r.descripcion,
      imagenUrl: r.imagenUrl || undefined,
      generos: r.generos,
      estado: 'PENDIENTE'
    }).subscribe({
      next: () => {
        this.explorarDetalle = null;
        this.explorarAgregando = false;
        this.cargarSeries();
      },
      error: () => { this.explorarAgregando = false; }
    });
  }

  constructor(private serieService: SerieService, private explorarService: ExplorarService) {}

  ngOnInit(): void {
    this.cargarSeries();
  }

  cargarSeries(): void {
    this.serieService.obtenerPorEstado('PENDIENTE').subscribe({
      next: (data) => this.pendientes = data,
      error: () => this.error = 'No se pudo conectar con el servidor'
    });
    this.serieService.obtenerPorEstado('EN_PROCESO').subscribe({
      next: (data) => this.enProceso = data
    });
    this.serieService.obtenerPorEstado('VISTA').subscribe({
      next: (data) => this.vistas = data
    });
  }

  // ── CREAR (solo título) ───────────────────────────
  crear(): void {
    if (!this.tituloNueva.trim()) return;
    this.serieService.crear({ titulo: this.tituloNueva.trim(), estado: 'PENDIENTE' }).subscribe({
      next: () => {
        this.tituloNueva = '';
        this.mostrarFormulario = false;
        this.cargarSeries();
      }
    });
  }

  // ── DETALLE ───────────────────────────────────────
  abrirDetalle(serie: Serie): void {
    this.serieDetalle = { ...serie };
  }

  // ── EDICIÓN ───────────────────────────────────────
  abrirEdicion(serie: Serie, event: Event): void {
    event.stopPropagation();
    this.serieDetalle = null;
    this.serieEditando = { ...serie, generos: [...(serie.generos || [])] };
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
    if (!this.serieEditando?.id) return;
    const guardar = (imagenUrl?: string) => {
      const datos = { ...this.serieEditando, ...(imagenUrl ? { imagenUrl } : {}) };
      this.serieService.editar(this.serieEditando!.id!, datos).subscribe({
        next: () => {
          this.serieEditando = null;
          this.imagenEditPreview = null;
          this.imagenEditSeleccionada = null;
          this.cargarSeries();
        }
      });
    };
    if (this.imagenEditSeleccionada) {
      this.serieService.subirImagen(this.imagenEditSeleccionada).subscribe({
        next: (res) => guardar(res.url),
        error: () => guardar()
      });
    } else {
      guardar();
    }
  }

  // ── MARCAR COMO VISTA (formulario completo) ───────
  abrirModalVista(serie: Serie, event?: Event): void {
    event?.stopPropagation();
    this.serieDetalle = null;
    this.serieSeleccionada = serie;
    this.vistaForm = {
      descripcion: serie.descripcion || '',
      imagenUrl: serie.imagenUrl,
      generos: [...(serie.generos || [])],
      nombrePersona1: serie.nombrePersona1 || '',
      personajeFavorito: serie.personajeFavorito || '',
      personajeOdiado: serie.personajeOdiado || '',
      nota: 5,
      nombrePersona2: serie.nombrePersona2 || '',
      personajeFavorito2: serie.personajeFavorito2 || '',
      personajeOdiado2: serie.personajeOdiado2 || '',
      nota2: undefined,
    };
    this.imagenVistaSeleccionada = null;
    this.imagenVistaPreview = null;
    this.mostrarGenerosVista = false;
  }

  onImagenVistaSeleccionada(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    this.imagenVistaSeleccionada = input.files[0];
    const reader = new FileReader();
    reader.onload = () => this.imagenVistaPreview = reader.result as string;
    reader.readAsDataURL(this.imagenVistaSeleccionada);
  }

  confirmarVista(): void {
    if (!this.serieSeleccionada?.id) return;
    const id = this.serieSeleccionada.id;
    const nota = this.vistaForm.nota ?? 5;

    const finalizar = (imagenUrl?: string) => {
      const datos: Partial<Serie> = {
        ...this.serieSeleccionada,
        ...this.vistaForm,
        ...(imagenUrl ? { imagenUrl } : {})
      };
      this.serieService.editar(id, datos as Serie).subscribe({
        next: () => {
          this.serieService.marcarComoVista(id, nota).subscribe({
            next: () => {
              this.serieSeleccionada = null;
              this.cargarSeries();
            },
            error: (e) => this.error = e.error?.error || 'Error al marcar como vista'
          });
        }
      });
    };

    if (this.imagenVistaSeleccionada) {
      this.serieService.subirImagen(this.imagenVistaSeleccionada).subscribe({
        next: (res) => finalizar(res.url),
        error: () => finalizar()
      });
    } else {
      finalizar();
    }
  }

  // ── ELIMINAR ──────────────────────────────────────
  eliminar(id: number, event: Event): void {
    event.stopPropagation();
    this.serieService.eliminar(id).subscribe({
      next: () => this.cargarSeries()
    });
  }

  // ── DRAG & DROP ───────────────────────────────────
  onDragStart(serie: Serie, event: DragEvent): void {
    event.dataTransfer!.setData('serieId', serie.id!.toString());
    event.dataTransfer!.setData('estadoOrigen', serie.estado);
    event.dataTransfer!.effectAllowed = 'move';
  }

  onDragOver(event: DragEvent, seccion: string): void {
    event.preventDefault();
    event.dataTransfer!.dropEffect = 'move';
    this.dragSobrePendiente = seccion === 'PENDIENTE';
    this.dragSobreEnProceso = seccion === 'EN_PROCESO';
    this.dragSobreVista     = seccion === 'VISTA';
  }

  onDragLeave(event: DragEvent, seccion: string): void {
    const zona = event.currentTarget as HTMLElement;
    const dest = event.relatedTarget as Node;
    if (dest && zona.contains(dest)) return;
    if (seccion === 'PENDIENTE') this.dragSobrePendiente = false;
    if (seccion === 'EN_PROCESO') this.dragSobreEnProceso = false;
    if (seccion === 'VISTA') this.dragSobreVista = false;
  }

  onDrop(event: DragEvent, destino: 'PENDIENTE' | 'EN_PROCESO' | 'VISTA'): void {
    event.preventDefault();
    this.dragSobrePendiente = this.dragSobreEnProceso = this.dragSobreVista = false;
    const id = parseInt(event.dataTransfer!.getData('serieId'), 10);
    const origen = event.dataTransfer!.getData('estadoOrigen');
    if (!id || origen === destino) return;

    if (destino === 'PENDIENTE') {
      this.serieService.marcarComoPendiente(id).subscribe({
        next: () => this.cargarSeries(),
        error: (e) => this.error = 'Error: ' + (e.error?.error || e.status)
      });
    } else if (destino === 'EN_PROCESO') {
      this.moverAEnProceso(id);
    } else {
      // VISTA → abrir formulario completo
      const todas = [...this.pendientes, ...this.enProceso, ...this.vistas];
      const serie = todas.find(s => s.id === id);
      if (serie) this.abrirModalVista(serie);
    }
  }

  moverAEnProceso(id: number): void {
    this.serieService.marcarComoEnProceso(id).subscribe({
      next: () => this.cargarSeries(),
      error: (e) => this.error = 'Error al mover: ' + (e.error?.error || e.status || 'revisa que el backend esté arrancado')
    });
  }

  // ── PROGRESO ──────────────────────────────────────
  cambiarProgreso(serie: Serie, campo: 'temporada' | 'episodio', delta: number): void {
    const t = (serie.temporadaActual ?? 1) + (campo === 'temporada' ? delta : 0);
    const e = (serie.episodioActual ?? 1) + (campo === 'episodio' ? delta : 0);
    if (t < 1 || e < 1) return;
    this.serieService.actualizarProgreso(serie.id!, t, e).subscribe({
      next: () => { serie.temporadaActual = t; serie.episodioActual = e; }
    });
  }

  // ── GÉNEROS ───────────────────────────────────────
  toggleGeneroVista(genero: string): void {
    const arr = this.vistaForm.generos || [];
    const idx = arr.indexOf(genero);
    if (idx >= 0) arr.splice(idx, 1); else arr.push(genero);
    this.vistaForm.generos = [...arr];
  }

  toggleGeneroEdicion(genero: string): void {
    if (!this.serieEditando) return;
    const arr = this.serieEditando.generos || [];
    const idx = arr.indexOf(genero);
    if (idx >= 0) arr.splice(idx, 1); else arr.push(genero);
    this.serieEditando.generos = [...arr];
  }

  tieneGenero(lista: string[] | undefined, genero: string): boolean {
    return !!(lista?.includes(genero));
  }

  imagenReal(url: string): string {
    return url.startsWith('http') ? url : 'http://localhost:8090' + url;
  }

  tienePersona2(serie: Serie): boolean {
    return !!(serie.nombrePersona2 || serie.personajeFavorito2 || serie.personajeOdiado2);
  }
}

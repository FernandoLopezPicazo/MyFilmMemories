import { Component, OnInit } from '@angular/core';
import { Pelicula, PeliculaService, Saga } from '../pelicula.service';
import { ExplorarService, ResultadoExplorar } from '../explorar.service';

const GENEROS_PELICULA = [
  'Acción', 'Aventura', 'Animación', 'Bélica', 'Ciencia Ficción', 'Comedia',
  'Crimen', 'Documental', 'Drama', 'Fantasía', 'Historia', 'Misterio',
  'Música', 'Romance', 'Suspenso', 'Terror', 'Western'
];

@Component({
  selector: 'app-peliculas-page',
  templateUrl: './peliculas-page.component.html',
  styleUrls: ['./peliculas-page.component.css']
})
export class PeliculasPageComponent implements OnInit {

  pendientes: Pelicula[] = [];
  vistas: Pelicula[] = [];
  dragSobrePendiente = false;
  dragSobreVista = false;
  error = '';

  // ── SAGAS ─────────────────────────────────────────
  sagas: Saga[] = [];
  seccionSagasAbierta = true;
  mostrarFormularioSaga = false;
  tituloNuevaSaga = '';
  sagasExpandidas = new Set<number>();
  tituloPeliEnSaga: { [sagaId: number]: string } = {};
  // Película de saga que se está marcando como vista
  peliculaSagaSeleccionada: Pelicula | null = null;
  sagaIdDeSeleccionada: number | null = null;
  // Arrastrar y soltar sobre una saga (vincular / reordenar)
  sagaIdDragSobre: number | null = null;

  // Formulario nueva película (solo título)
  tituloNueva = '';
  mostrarFormulario = false;

  // Buscador y filtros
  busqueda = '';
  generosSeleccionados: string[] = [];
  mostrarFiltro = false;

  get pendientesFiltradas() { return this.filtrar(this.pendientes); }
  get vistasFiltradas()     { return this.filtrar(this.vistas); }

  private filtrar(lista: Pelicula[]): Pelicula[] {
    let resultado = lista;
    const q = this.busqueda.trim().toLowerCase();
    if (q) resultado = resultado.filter(p => p.titulo.toLowerCase().includes(q));
    if (this.generosSeleccionados.length > 0) {
      resultado = resultado.filter(p =>
        this.generosSeleccionados.some(g => p.generos?.includes(g))
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
  peliculaDetalle: Pelicula | null = null;

  // Modal marcar como vista (formulario completo)
  peliculaSeleccionada: Pelicula | null = null;
  vistaForm: Partial<Pelicula> = {};
  imagenVistaSeleccionada: File | null = null;
  imagenVistaPreview: string | null = null;
  mostrarGenerosVista = false;

  // Modal edición
  peliculaEditando: Pelicula | null = null;
  imagenEditPreview: string | null = null;
  imagenEditSeleccionada: File | null = null;
  mostrarGenerosEdit = false;

  readonly GENEROS = GENEROS_PELICULA;

  // ── HORARIO ────────────────────────────────────────
  readonly FRECUENCIAS: { valor: NonNullable<Pelicula['frecuencia']>; etiqueta: string }[] = [
    { valor: 'SEMANAL', etiqueta: 'Semanal' },
    { valor: 'MENSUAL', etiqueta: 'Mensual' }
  ];
  readonly DIAS_SEMANA: { valor: NonNullable<Pelicula['diaSemana']>; etiqueta: string }[] = [
    { valor: 'LUNES', etiqueta: 'Lunes' }, { valor: 'MARTES', etiqueta: 'Martes' },
    { valor: 'MIERCOLES', etiqueta: 'Miércoles' }, { valor: 'JUEVES', etiqueta: 'Jueves' },
    { valor: 'VIERNES', etiqueta: 'Viernes' }, { valor: 'SABADO', etiqueta: 'Sábado' },
    { valor: 'DOMINGO', etiqueta: 'Domingo' }
  ];
  readonly SEMANAS_MES = [1, 2, 3, 4, 5];

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
  explorarError = '';

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
    this.explorarError = '';
    this.explorarService.descubrirPeliculas(this.explorarGenerosActivos, 1).subscribe({
      next: (r) => { this.explorarResultados = r; this.explorarCargando = false; },
      error: () => { this.explorarCargando = false; this.explorarError = this.mensajeErrorExplorar(); }
    });
  }

  abrirExplorar(): void {
    this.mostrarExplorar = true;
    this.explorarBusqueda = '';
    this.explorarGenerosActivos = [];
    this.explorarPagina = 1;
    this.explorarCargando = true;
    this.explorarError = '';
    this.explorarService.trendingPeliculas(1).subscribe({
      next: (r) => { this.explorarResultados = r; this.explorarCargando = false; },
      error: () => { this.explorarCargando = false; this.explorarError = this.mensajeErrorExplorar(); }
    });
  }

  buscarExplorar(): void {
    const q = this.explorarBusqueda.trim();
    if (!q) { this.abrirExplorar(); return; }
    this.explorarPagina = 1;
    this.explorarCargando = true;
    this.explorarError = '';
    this.explorarService.buscarPeliculas(q, 1).subscribe({
      next: (r) => { this.explorarResultados = r; this.explorarCargando = false; },
      error: () => { this.explorarCargando = false; this.explorarError = this.mensajeErrorExplorar(); }
    });
  }

  cargarMasExplorar(): void {
    this.explorarPagina++;
    this.explorarCargandoMas = true;
    this.explorarError = '';
    const q = this.explorarBusqueda.trim();
    const obs = this.explorarGenerosActivos.length
      ? this.explorarService.descubrirPeliculas(this.explorarGenerosActivos, this.explorarPagina)
      : q
        ? this.explorarService.buscarPeliculas(q, this.explorarPagina)
        : this.explorarService.trendingPeliculas(this.explorarPagina);
    obs.subscribe({
      next: (r) => { this.explorarResultados = [...this.explorarResultados, ...r]; this.explorarCargandoMas = false; },
      error: () => { this.explorarPagina--; this.explorarCargandoMas = false; this.explorarError = this.mensajeErrorExplorar(); }
    });
  }

  // TMDB puede fallar por caida/rate-limit — sin esto, el fallo se veia
  // igual que "no hay resultados" y el usuario no sabia que era temporal.
  private mensajeErrorExplorar(): string {
    return 'No se pudo conectar con el buscador. Puede ser un problema temporal del servicio externo — inténtalo de nuevo en un momento.';
  }

  abrirDetalleExplorar(r: ResultadoExplorar): void {
    this.explorarDetalle = r;
    this.explorarAgregando = false;
  }

  agregarDesdeExplorar(r: ResultadoExplorar): void {
    this.explorarAgregando = true;
    this.peliculaService.crear({
      titulo: r.titulo,
      descripcion: r.descripcion,
      imagenUrl: r.imagenUrl || undefined,
      generos: r.generos,
      estado: 'PENDIENTE'
    }).subscribe({
      next: () => {
        this.explorarDetalle = null;
        this.explorarAgregando = false;
        this.cargarPeliculas();
      },
      error: () => { this.explorarAgregando = false; }
    });
  }

  constructor(public peliculaService: PeliculaService, private explorarService: ExplorarService) {}

  ngOnInit(): void {
    this.cargarPeliculas();
    this.cargarSagas();
  }

  cargarSagas(): void {
    this.peliculaService.obtenerSagas().subscribe({
      next: (data) => this.sagas = data
    });
  }

  // ── SAGAS ─────────────────────────────────────────
  crearSaga(): void {
    if (!this.tituloNuevaSaga.trim()) return;
    this.peliculaService.crearSaga(this.tituloNuevaSaga.trim()).subscribe({
      next: () => {
        this.tituloNuevaSaga = '';
        this.mostrarFormularioSaga = false;
        this.cargarSagas();
      }
    });
  }

  eliminarSaga(id: number): void {
    this.peliculaService.eliminarSaga(id).subscribe({
      next: () => {
        this.sagasExpandidas.delete(id);
        this.cargarSagas();
      }
    });
  }

  toggleSaga(id: number): void {
    if (this.sagasExpandidas.has(id)) this.sagasExpandidas.delete(id);
    else this.sagasExpandidas.add(id);
  }

  agregarPeliASaga(sagaId: number): void {
    const titulo = (this.tituloPeliEnSaga[sagaId] || '').trim();
    if (!titulo) return;
    this.peliculaService.agregarPeliculaASaga(sagaId, titulo).subscribe({
      next: () => {
        this.tituloPeliEnSaga[sagaId] = '';
        this.cargarSagas();
      }
    });
  }

  abrirVistaEnSaga(pelicula: Pelicula, sagaId: number, event: Event): void {
    event.stopPropagation();
    this.sagaIdDeSeleccionada = sagaId;
    this.abrirModalVista(pelicula, event);
  }

  deshacerVistaSaga(peliculaId: number, event: Event): void {
    event.stopPropagation();
    this.peliculaService.marcarComoPendiente(peliculaId).subscribe({
      next: () => this.cargarSagas()
    });
  }

  eliminarPeliDeSaga(peliculaId: number, event: Event): void {
    event.stopPropagation();
    this.peliculaService.eliminar(peliculaId).subscribe({
      next: () => this.cargarSagas()
    });
  }

  contarVistas(saga: Saga): number {
    return saga.peliculas?.filter(p => p.estado === 'VISTA').length ?? 0;
  }

  cargarPeliculas(): void {
    this.peliculaService.obtenerPorEstado('PENDIENTE').subscribe({
      next: (data) => this.pendientes = data,
      error: () => this.error = 'No se pudo conectar con el servidor'
    });
    this.peliculaService.obtenerPorEstado('VISTA').subscribe({
      next: (data) => this.vistas = data
    });
  }

  // ── CREAR (solo título) ───────────────────────────
  crear(): void {
    if (!this.tituloNueva.trim()) return;
    this.peliculaService.crear({ titulo: this.tituloNueva.trim(), estado: 'PENDIENTE' }).subscribe({
      next: () => {
        this.tituloNueva = '';
        this.mostrarFormulario = false;
        this.cargarPeliculas();
      }
    });
  }

  // ── DETALLE ───────────────────────────────────────
  abrirDetalle(pelicula: Pelicula): void {
    this.peliculaDetalle = { ...pelicula };
  }

  // ── EDICIÓN ───────────────────────────────────────
  abrirEdicion(pelicula: Pelicula, event: Event): void {
    event.stopPropagation();
    this.peliculaDetalle = null;
    this.peliculaEditando = { ...pelicula, generos: [...(pelicula.generos || [])] };
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
    if (!this.peliculaEditando?.id) return;
    const guardar = (imagenUrl?: string) => {
      const datos = { ...this.peliculaEditando, ...(imagenUrl ? { imagenUrl } : {}) };
      this.peliculaService.editar(this.peliculaEditando!.id!, datos).subscribe({
        next: () => {
          this.peliculaEditando = null;
          this.imagenEditPreview = null;
          this.imagenEditSeleccionada = null;
          this.cargarPeliculas();
        }
      });
    };
    if (this.imagenEditSeleccionada) {
      this.peliculaService.subirImagen(this.imagenEditSeleccionada).subscribe({
        next: (res) => guardar(res.url),
        error: () => guardar()
      });
    } else {
      guardar();
    }
  }

  // ── MARCAR COMO VISTA (formulario completo) ───────
  abrirModalVista(pelicula: Pelicula, event?: Event): void {
    event?.stopPropagation();
    this.peliculaDetalle = null;
    this.peliculaSeleccionada = pelicula;
    this.vistaForm = {
      descripcion: pelicula.descripcion || '',
      imagenUrl: pelicula.imagenUrl,
      generos: [...(pelicula.generos || [])],
      duracionMinutos: pelicula.duracionMinutos,
      nombrePersona1: pelicula.nombrePersona1 || '',
      personajeFavorito: pelicula.personajeFavorito || '',
      personajeOdiado: pelicula.personajeOdiado || '',
      nota: 5,
      nombrePersona2: pelicula.nombrePersona2 || '',
      personajeFavorito2: pelicula.personajeFavorito2 || '',
      personajeOdiado2: pelicula.personajeOdiado2 || '',
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
    if (!this.peliculaSeleccionada?.id) return;
    const id = this.peliculaSeleccionada.id;
    const nota = this.vistaForm.nota ?? 5;

    const finalizar = (imagenUrl?: string) => {
      const datos: Partial<Pelicula> = {
        ...this.peliculaSeleccionada,
        ...this.vistaForm,
        ...(imagenUrl ? { imagenUrl } : {})
      };
      this.peliculaService.editar(id, datos as Pelicula).subscribe({
        next: () => {
          this.peliculaService.marcarComoVista(id, nota).subscribe({
            next: () => {
              this.peliculaSeleccionada = null;
              this.cargarPeliculas();
              this.cargarSagas();
            },
            error: (e) => this.error = e.error?.error || 'Error al marcar como vista'
          });
        }
      });
    };

    if (this.imagenVistaSeleccionada) {
      this.peliculaService.subirImagen(this.imagenVistaSeleccionada).subscribe({
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
    this.peliculaService.eliminar(id).subscribe({
      next: () => this.cargarPeliculas()
    });
  }

  alternarVisibilidad(pelicula: Pelicula, event: Event): void {
    event.stopPropagation();
    this.peliculaService.alternarVisibilidad(pelicula.id!).subscribe(() => {
      pelicula.ocultoParaAmigos = !pelicula.ocultoParaAmigos;
    });
  }

  // ── DRAG & DROP ────────────────────────────────────
  // El origen viaja como 'PENDIENTE' | 'VISTA' (columnas sueltas) o
  // 'SAGA:<sagaId>' (fila dentro de una saga) para que cualquier zona de
  // destino sepa si tiene que desvincular de una saga antes de aplicar
  // su propia lógica.
  onDragStart(pelicula: Pelicula, event: DragEvent): void {
    event.dataTransfer!.setData('peliculaId', pelicula.id!.toString());
    event.dataTransfer!.setData('estadoOrigen', pelicula.sagaId != null ? `SAGA:${pelicula.sagaId}` : pelicula.estado);
    event.dataTransfer!.effectAllowed = 'move';
  }

  onDragOver(event: DragEvent, seccion: string): void {
    event.preventDefault();
    event.dataTransfer!.dropEffect = 'move';
    this.dragSobrePendiente = seccion === 'PENDIENTE';
    this.dragSobreVista = seccion === 'VISTA';
  }

  onDragLeave(event: DragEvent, seccion: string): void {
    const zona = event.currentTarget as HTMLElement;
    const dest = event.relatedTarget as Node;
    if (dest && zona.contains(dest)) return;
    if (seccion === 'PENDIENTE') this.dragSobrePendiente = false;
    if (seccion === 'VISTA') this.dragSobreVista = false;
  }

  onDrop(event: DragEvent, destino: 'PENDIENTE' | 'VISTA'): void {
    event.preventDefault();
    this.dragSobrePendiente = this.dragSobreVista = false;
    const id = parseInt(event.dataTransfer!.getData('peliculaId'), 10);
    const origen = event.dataTransfer!.getData('estadoOrigen');
    const vieneDeSaga = origen.startsWith('SAGA:');
    if (!id || (!vieneDeSaga && origen === destino)) return;

    const continuar = () => {
      if (destino === 'PENDIENTE') {
        this.peliculaService.marcarComoPendiente(id).subscribe({
          next: () => this.cargarPeliculas(),
          error: (e) => this.error = 'Error: ' + (e.error?.error || e.status)
        });
      } else {
        // VISTA → abrir formulario completo
        const todas = [...this.pendientes, ...this.vistas];
        const pelicula = todas.find(p => p.id === id);
        if (pelicula) this.abrirModalVista(pelicula);
      }
    };

    if (vieneDeSaga) {
      this.peliculaService.quitarPeliculaDeSaga(id).subscribe({
        next: () => { this.cargarSagas(); continuar(); },
        error: (e) => this.error = 'Error: ' + (e.error?.error || e.status)
      });
    } else {
      continuar();
    }
  }

  // ── DRAG & DROP: vincular a saga (soltar sobre la tarjeta) ──
  onDragOverSaga(event: DragEvent, sagaId: number): void {
    event.preventDefault();
    event.dataTransfer!.dropEffect = 'move';
    this.sagaIdDragSobre = sagaId;
  }

  onDragLeaveSaga(event: DragEvent, sagaId: number): void {
    const zona = event.currentTarget as HTMLElement;
    const dest = event.relatedTarget as Node;
    if (dest && zona.contains(dest)) return;
    if (this.sagaIdDragSobre === sagaId) this.sagaIdDragSobre = null;
  }

  onDropEnSaga(event: DragEvent, sagaId: number): void {
    event.preventDefault();
    event.stopPropagation();
    this.sagaIdDragSobre = null;
    const id = parseInt(event.dataTransfer!.getData('peliculaId'), 10);
    const origen = event.dataTransfer!.getData('estadoOrigen');
    if (!id || origen === `SAGA:${sagaId}`) return; // ya está en esta saga

    this.peliculaService.vincularPeliculaASaga(sagaId, id).subscribe({
      next: () => { this.cargarSagas(); this.cargarPeliculas(); },
      error: (e) => this.error = 'Error: ' + (e.error?.error || e.status)
    });
  }

  // ── DRAG & DROP: reordenar dentro de una saga ──
  onDragOverSagaFila(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    event.dataTransfer!.dropEffect = 'move';
  }

  onDropEnSagaFila(event: DragEvent, sagaId: number, peliculaDestinoId: number): void {
    event.preventDefault();
    event.stopPropagation();
    this.sagaIdDragSobre = null;
    const id = parseInt(event.dataTransfer!.getData('peliculaId'), 10);
    const origen = event.dataTransfer!.getData('estadoOrigen');
    if (!id || id === peliculaDestinoId) return;

    const saga = this.sagas.find(s => s.id === sagaId);
    if (!saga) return;

    const idsActuales = (saga.peliculas || []).map(p => p.id!).filter(pid => pid !== id);
    const posicion = idsActuales.indexOf(peliculaDestinoId);
    idsActuales.splice(posicion < 0 ? idsActuales.length : posicion, 0, id);

    const aplicarOrden = () => {
      this.peliculaService.reordenarSaga(sagaId, idsActuales).subscribe({
        next: () => { this.cargarSagas(); this.cargarPeliculas(); },
        error: (e) => this.error = 'Error: ' + (e.error?.error || e.status)
      });
    };

    if (origen === `SAGA:${sagaId}`) {
      aplicarOrden();
    } else {
      // Viene de fuera de esta saga: primero vincular, luego aplicar el orden completo
      this.peliculaService.vincularPeliculaASaga(sagaId, id).subscribe({
        next: () => aplicarOrden(),
        error: (e) => this.error = 'Error: ' + (e.error?.error || e.status)
      });
    }
  }

  // ── GÉNEROS ───────────────────────────────────────
  toggleGeneroVista(genero: string): void {
    const arr = this.vistaForm.generos || [];
    const idx = arr.indexOf(genero);
    if (idx >= 0) arr.splice(idx, 1); else arr.push(genero);
    this.vistaForm.generos = [...arr];
  }

  toggleGeneroEdicion(genero: string): void {
    if (!this.peliculaEditando) return;
    const arr = this.peliculaEditando.generos || [];
    const idx = arr.indexOf(genero);
    if (idx >= 0) arr.splice(idx, 1); else arr.push(genero);
    this.peliculaEditando.generos = [...arr];
  }

  tieneGenero(lista: string[] | undefined, genero: string): boolean {
    return !!(lista?.includes(genero));
  }

  imagenReal(url: string): string {
    return url.startsWith('http') ? url : 'http://localhost:8090' + url;
  }

  tienePersona2(pelicula: Pelicula): boolean {
    return !!(pelicula.nombrePersona2 || pelicula.personajeFavorito2 || pelicula.personajeOdiado2);
  }
}

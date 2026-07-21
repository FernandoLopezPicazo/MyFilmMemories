import { Component, OnInit } from '@angular/core';
import { AuthService } from '../auth.service';
import {
  GrupoService, Grupo, GrupoDetalle, GrupoInvitacion, GrupoItem, TipoGrupoItem
} from '../grupo.service';
import { ExplorarService, ResultadoExplorar } from '../explorar.service';

@Component({
  selector: 'app-grupos-page',
  templateUrl: './grupos-page.component.html',
  styleUrls: ['./grupos-page.component.css']
})
export class GruposPageComponent implements OnInit {

  grupos: Grupo[] = [];
  invitaciones: GrupoInvitacion[] = [];
  nombreNuevoGrupo = '';
  error = '';

  grupoSeleccionado: GrupoDetalle | null = null;
  tabItem: TipoGrupoItem = 'SERIE';
  items: GrupoItem[] = [];

  mostrarFormularioItem = false;
  nuevoTitulo = '';
  nuevaDescripcion = '';
  nuevaImagenUrl: string | null = null;
  nuevosGeneros: string[] = [];

  mostrarBusquedaManual = false;
  busquedaExplorar = '';
  resultadosExplorar: ResultadoExplorar[] = [];
  cargandoExplorar = false;

  emailInvitar = '';

  itemAbierto: number | null = null;
  formOpinion = { nota: null as number | null, personajeFavorito: '', personajeOdiado: '', comentario: '' };

  constructor(private grupoService: GrupoService, private explorarService: ExplorarService, public auth: AuthService) {}

  ngOnInit(): void {
    this.cargarGrupos();
    this.grupoService.invitacionesRecibidas().subscribe(i => this.invitaciones = i);
  }

  cargarGrupos(): void {
    this.grupoService.misGrupos().subscribe(g => this.grupos = g);
  }

  crearGrupo(): void {
    if (!this.nombreNuevoGrupo.trim()) return;
    this.grupoService.crear(this.nombreNuevoGrupo.trim()).subscribe({
      next: () => { this.nombreNuevoGrupo = ''; this.cargarGrupos(); },
      error: (e) => this.error = e?.error?.error || 'No se pudo crear el grupo'
    });
  }

  aceptarInvitacion(inv: GrupoInvitacion): void {
    this.grupoService.aceptarInvitacion(inv.id).subscribe(() => {
      this.invitaciones = this.invitaciones.filter(i => i.id !== inv.id);
      this.cargarGrupos();
    });
  }

  rechazarInvitacion(inv: GrupoInvitacion): void {
    this.grupoService.rechazarInvitacion(inv.id).subscribe(() => {
      this.invitaciones = this.invitaciones.filter(i => i.id !== inv.id);
    });
  }

  abrirGrupo(grupo: Grupo): void {
    this.grupoService.detalle(grupo.id).subscribe(d => {
      this.grupoSeleccionado = d;
      this.tabItem = 'SERIE';
      this.cargarItems();
    });
  }

  volverALista(): void {
    this.grupoSeleccionado = null;
    this.items = [];
    this.itemAbierto = null;
  }

  cambiarTabItem(tipo: TipoGrupoItem): void {
    this.tabItem = tipo;
    this.itemAbierto = null;
    this.mostrarFormularioItem = false;
    this.resultadosExplorar = [];
    this.cargarItems();
  }

  private cargarItems(): void {
    if (!this.grupoSeleccionado) return;
    this.grupoService.listarItems(this.grupoSeleccionado.id, this.tabItem).subscribe(i => this.items = i);
  }

  abrirFormularioItem(): void {
    this.mostrarFormularioItem = !this.mostrarFormularioItem;
    this.mostrarBusquedaManual = false;
    this.busquedaExplorar = '';
    this.resultadosExplorar = [];
    this.nuevoTitulo = '';
    this.nuevaDescripcion = '';
    this.nuevaImagenUrl = null;
    this.nuevosGeneros = [];
  }

  buscarExplorarGrupo(): void {
    const q = this.busquedaExplorar.trim();
    if (!q) { this.resultadosExplorar = []; return; }
    this.cargandoExplorar = true;
    const obs = this.tabItem === 'SERIE' ? this.explorarService.buscarSeries(q)
              : this.tabItem === 'PELICULA' ? this.explorarService.buscarPeliculas(q)
              : this.explorarService.buscarMangas(q);
    obs.subscribe({
      next: (r) => { this.resultadosExplorar = r; this.cargandoExplorar = false; },
      error: () => { this.cargandoExplorar = false; }
    });
  }

  agregarDesdeExplorar(r: ResultadoExplorar): void {
    if (!this.grupoSeleccionado) return;
    this.grupoService.crearItem(this.grupoSeleccionado.id, this.tabItem, r.titulo, r.descripcion,
        r.generos, r.imagenUrl).subscribe(() => {
      this.mostrarFormularioItem = false;
      this.resultadosExplorar = [];
      this.busquedaExplorar = '';
      this.cargarItems();
    });
  }

  crearItem(): void {
    if (!this.grupoSeleccionado || !this.nuevoTitulo.trim()) return;
    this.grupoService.crearItem(this.grupoSeleccionado.id, this.tabItem, this.nuevoTitulo.trim(),
        this.nuevaDescripcion.trim(), this.nuevosGeneros, this.nuevaImagenUrl).subscribe(() => {
      this.nuevoTitulo = '';
      this.nuevaDescripcion = '';
      this.nuevaImagenUrl = null;
      this.nuevosGeneros = [];
      this.mostrarFormularioItem = false;
      this.cargarItems();
    });
  }

  eliminarItem(item: GrupoItem, evento: Event): void {
    evento.stopPropagation();
    if (!this.grupoSeleccionado) return;
    if (!confirm(`¿Eliminar "${item.titulo}" de la lista del grupo?`)) return;
    this.grupoService.eliminarItem(this.grupoSeleccionado.id, item.id).subscribe(() => this.cargarItems());
  }

  invitar(): void {
    if (!this.grupoSeleccionado || !this.emailInvitar.trim()) return;
    this.error = '';
    this.grupoService.invitar(this.grupoSeleccionado.id, this.emailInvitar.trim()).subscribe({
      next: () => { this.emailInvitar = ''; },
      error: (e) => this.error = e?.error?.error || 'No se pudo invitar'
    });
  }

  salirDelGrupo(): void {
    if (!this.grupoSeleccionado) return;
    if (!confirm('¿Salir de este grupo?')) return;
    this.grupoService.salir(this.grupoSeleccionado.id).subscribe(() => {
      this.volverALista();
      this.cargarGrupos();
    });
  }

  abrirOpinion(item: GrupoItem): void {
    if (this.itemAbierto === item.id) {
      this.itemAbierto = null;
      return;
    }
    this.itemAbierto = item.id;
    const miOpinion = item.opiniones.find(o => o.email === this.auth.email);
    this.formOpinion = {
      nota: miOpinion?.nota ?? null,
      personajeFavorito: miOpinion?.personajeFavorito || '',
      personajeOdiado: miOpinion?.personajeOdiado || '',
      comentario: miOpinion?.comentario || ''
    };
  }

  guardarOpinion(item: GrupoItem): void {
    if (!this.grupoSeleccionado) return;
    this.grupoService.opinar(this.grupoSeleccionado.id, item.id, this.formOpinion.nota,
        this.formOpinion.personajeFavorito, this.formOpinion.personajeOdiado, this.formOpinion.comentario)
      .subscribe(() => {
        this.itemAbierto = null;
        this.cargarItems();
      });
  }

  esMiOpinion(email: string): boolean {
    return email === this.auth.email;
  }
}

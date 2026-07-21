import { Component, OnInit } from '@angular/core';
import { AmigoService, Amigo, SolicitudAmistad } from '../amigo.service';
import { Serie } from '../serie.service';
import { Pelicula } from '../pelicula.service';
import { Manga } from '../manga.service';

type TabColeccion = 'series' | 'peliculas' | 'mangas';

@Component({
  selector: 'app-amigos-page',
  templateUrl: './amigos-page.component.html',
  styleUrls: ['./amigos-page.component.css']
})
export class AmigosPageComponent implements OnInit {

  amigos: Amigo[] = [];
  recibidas: SolicitudAmistad[] = [];
  enviadas: SolicitudAmistad[] = [];

  emailBusqueda = '';
  error = '';
  mensaje = '';
  cargando = false;

  amigoSeleccionado: Amigo | null = null;
  tabColeccion: TabColeccion = 'series';
  seriesAmigo: Serie[] = [];
  peliculasAmigo: Pelicula[] = [];
  mangasAmigo: Manga[] = [];

  constructor(private amigoService: AmigoService) {}

  ngOnInit(): void {
    this.cargarTodo();
  }

  cargarTodo(): void {
    this.amigoService.listarAmigos().subscribe(a => this.amigos = a);
    this.amigoService.listarRecibidas().subscribe(r => this.recibidas = r);
    this.amigoService.listarEnviadas().subscribe(e => this.enviadas = e);
  }

  enviarSolicitud(): void {
    if (!this.emailBusqueda.trim()) return;
    this.error = '';
    this.mensaje = '';
    this.cargando = true;
    this.amigoService.enviarSolicitud(this.emailBusqueda.trim()).subscribe({
      next: () => {
        this.mensaje = 'Solicitud enviada';
        this.emailBusqueda = '';
        this.cargando = false;
        this.cargarTodo();
      },
      error: (e) => {
        this.error = e?.error?.error || 'No se pudo enviar la solicitud';
        this.cargando = false;
      }
    });
  }

  aceptar(solicitud: SolicitudAmistad): void {
    this.amigoService.aceptar(solicitud.id).subscribe(() => this.cargarTodo());
  }

  rechazar(solicitud: SolicitudAmistad): void {
    this.amigoService.rechazar(solicitud.id).subscribe(() => this.cargarTodo());
  }

  eliminarAmistad(amigo: Amigo): void {
    if (!confirm(`¿Dejar de ser amigo de ${amigo.email}?`)) return;
    this.amigoService.eliminarAmistad(amigo.usuarioId).subscribe(() => {
      if (this.amigoSeleccionado?.usuarioId === amigo.usuarioId) this.amigoSeleccionado = null;
      this.cargarTodo();
    });
  }

  verColeccion(amigo: Amigo): void {
    this.amigoSeleccionado = amigo;
    this.tabColeccion = 'series';
    this.cargarColeccion();
  }

  cambiarTab(tab: TabColeccion): void {
    this.tabColeccion = tab;
    this.cargarColeccion();
  }

  private cargarColeccion(): void {
    if (!this.amigoSeleccionado) return;
    const id = this.amigoSeleccionado.usuarioId;
    if (this.tabColeccion === 'series') {
      this.amigoService.seriesDeAmigo(id).subscribe(s => this.seriesAmigo = s);
    } else if (this.tabColeccion === 'peliculas') {
      this.amigoService.peliculasDeAmigo(id).subscribe(p => this.peliculasAmigo = p);
    } else {
      this.amigoService.mangasDeAmigo(id).subscribe(m => this.mangasAmigo = m);
    }
  }

  cerrarColeccion(): void {
    this.amigoSeleccionado = null;
  }
}

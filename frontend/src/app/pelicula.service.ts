import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface Saga {
  id?: number;
  titulo: string;
  estado: 'EN_PROCESO' | 'FINALIZADA';
  peliculas?: Pelicula[];
}

export interface Pelicula {
  id?: number;
  titulo: string;
  descripcion?: string;
  imagenUrl?: string;
  // Persona 1
  nombrePersona1?: string;
  personajeFavorito?: string;
  personajeOdiado?: string;
  nota?: number;
  // Persona 2
  nombrePersona2?: string;
  personajeFavorito2?: string;
  personajeOdiado2?: string;
  nota2?: number;
  generos?: string[];
  estado: 'PENDIENTE'| 'VISTA';
  fechaVista?: string;
  duracionMinutos?: number; // Duración en minutos
  ocultoParaAmigos?: boolean;
}

@Injectable({ providedIn: 'root' })
export class PeliculaService {

  private apiUrl = `${environment.apiUrl}/api/peliculas`;
  private imagenesUrl = `${environment.apiUrl}/api/imagenes`;

  constructor(private http: HttpClient) {}

  obtenerPorEstado(estado: string): Observable<Pelicula[]> {
    return this.http.get<Pelicula[]>(`${this.apiUrl}?estado=${estado}`);
  }

  obtenerTodas(): Observable<Pelicula[]> {
    return this.http.get<Pelicula[]>(this.apiUrl);
  }

  crear(pelicula: Partial<Pelicula>): Observable<Pelicula> {
    return this.http.post<Pelicula>(this.apiUrl, pelicula);
  }

  editar(id: number, pelicula: Partial<Pelicula>): Observable<Pelicula> {
    return this.http.put<Pelicula>(`${this.apiUrl}/${id}`, pelicula);
  }

  marcarComoVista(id: number, nota: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/vista`, { nota });
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  marcarComoPendiente(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/pendiente`, {});
  }


  // ── SAGAS ─────────────────────────────────────────
  private sagasUrl = `${environment.apiUrl}/api/sagas`;

  obtenerSagas(): Observable<Saga[]> {
    return this.http.get<Saga[]>(this.sagasUrl);
  }

  crearSaga(titulo: string): Observable<Saga> {
    return this.http.post<Saga>(this.sagasUrl, { titulo });
  }

  eliminarSaga(id: number): Observable<void> {
    return this.http.delete<void>(`${this.sagasUrl}/${id}`);
  }

  agregarPeliculaASaga(sagaId: number, titulo: string): Observable<Pelicula> {
    return this.http.post<Pelicula>(`${this.sagasUrl}/${sagaId}/peliculas`, { titulo });
  }

  quitarPeliculaDeSaga(peliculaId: number): Observable<void> {
    return this.http.delete<void>(`${this.sagasUrl}/peliculas/${peliculaId}`);
  }

  subirImagen(archivo: File): Observable<{ url: string }> {
    const formData = new FormData();
    formData.append('archivo', archivo);
    return this.http.post<{ url: string }>(`${this.imagenesUrl}/subir`, formData);
  }

  alternarVisibilidad(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/visibilidad`, {});
  }
}

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface Serie {
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
  estado: 'PENDIENTE' | 'EN_PROCESO' | 'VISTA';
  fechaVista?: string;
  temporadaActual?: number;
  episodioActual?: number;
  ocultoParaAmigos?: boolean;
  enEmision?: boolean;
  frecuencia?: 'SEMANAL' | 'MENSUAL';
  diaSemana?: 'LUNES' | 'MARTES' | 'MIERCOLES' | 'JUEVES' | 'VIERNES' | 'SABADO' | 'DOMINGO';
  semanaDelMes?: number;
}

@Injectable({ providedIn: 'root' })
export class SerieService {

  private apiUrl = `${environment.apiUrl}/api/series`;
  private imagenesUrl = `${environment.apiUrl}/api/imagenes`;

  constructor(private http: HttpClient) {}

  obtenerPorEstado(estado: string): Observable<Serie[]> {
    return this.http.get<Serie[]>(`${this.apiUrl}?estado=${estado}`);
  }

  obtenerTodas(): Observable<Serie[]> {
    return this.http.get<Serie[]>(this.apiUrl);
  }

  crear(serie: Partial<Serie>): Observable<Serie> {
    return this.http.post<Serie>(this.apiUrl, serie);
  }

  editar(id: number, serie: Partial<Serie>): Observable<Serie> {
    return this.http.put<Serie>(`${this.apiUrl}/${id}`, serie);
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

  marcarComoEnProceso(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/proceso`, {});
  }

  actualizarProgreso(id: number, temporada: number, episodio: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/progreso`, { temporada, episodio });
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

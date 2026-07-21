import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { Serie } from './serie.service';
import { Pelicula } from './pelicula.service';
import { Manga } from './manga.service';

export interface Amigo {
  usuarioId: string;
  email: string;
}

export interface SolicitudAmistad {
  id: number;
  otroUsuarioId: string;
  otroEmail: string;
  fechaSolicitud: string;
}

@Injectable({ providedIn: 'root' })
export class AmigoService {

  private url = `${environment.apiUrl}/api/amigos`;

  constructor(private http: HttpClient) {}

  enviarSolicitud(email: string): Observable<void> {
    return this.http.post<void>(`${this.url}/solicitudes`, { email });
  }

  listarRecibidas(): Observable<SolicitudAmistad[]> {
    return this.http.get<SolicitudAmistad[]>(`${this.url}/solicitudes/recibidas`);
  }

  listarEnviadas(): Observable<SolicitudAmistad[]> {
    return this.http.get<SolicitudAmistad[]>(`${this.url}/solicitudes/enviadas`);
  }

  aceptar(solicitudId: number): Observable<void> {
    return this.http.put<void>(`${this.url}/solicitudes/${solicitudId}/aceptar`, {});
  }

  rechazar(solicitudId: number): Observable<void> {
    return this.http.put<void>(`${this.url}/solicitudes/${solicitudId}/rechazar`, {});
  }

  listarAmigos(): Observable<Amigo[]> {
    return this.http.get<Amigo[]>(this.url);
  }

  eliminarAmistad(usuarioId: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${usuarioId}`);
  }

  seriesDeAmigo(usuarioId: string): Observable<Serie[]> {
    return this.http.get<Serie[]>(`${this.url}/${usuarioId}/series`);
  }

  peliculasDeAmigo(usuarioId: string): Observable<Pelicula[]> {
    return this.http.get<Pelicula[]>(`${this.url}/${usuarioId}/peliculas`);
  }

  mangasDeAmigo(usuarioId: string): Observable<Manga[]> {
    return this.http.get<Manga[]>(`${this.url}/${usuarioId}/mangas`);
  }
}

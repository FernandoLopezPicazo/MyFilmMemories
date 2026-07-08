import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Manga {
  id?: number;
  titulo: string;
  descripcion?: string;
  imagenUrl?: string;
  capituloActual?: number;
  urlLectura?: string;
  nombrePersona1?: string;
  personajeFavorito?: string;
  personajeOdiado?: string;
  nota?: number;
  nombrePersona2?: string;
  personajeFavorito2?: string;
  personajeOdiado2?: string;
  nota2?: number;
  estado: 'PENDIENTE' | 'EN_PROCESO' | 'FINALIZADO';
  fechaFinalizado?: string;
  generos?: string[];
}

@Injectable({ providedIn: 'root' })
export class MangaService {

  private url = 'http://localhost:8090/api/mangas';

  constructor(private http: HttpClient) {}

  obtenerPorEstado(estado: string): Observable<Manga[]> {
    return this.http.get<Manga[]>(`${this.url}?estado=${estado}`);
  }

  crear(manga: Partial<Manga>): Observable<Manga> {
    return this.http.post<Manga>(this.url, manga);
  }

  marcarComoEnProceso(id: number): Observable<void> {
    return this.http.put<void>(`${this.url}/${id}/proceso`, {});
  }

  actualizarProgreso(id: number, capituloActual: number, urlLectura: string): Observable<void> {
    return this.http.put<void>(`${this.url}/${id}/progreso`, { capituloActual, urlLectura });
  }

  finalizar(id: number, datos: any): Observable<void> {
    return this.http.put<void>(`${this.url}/${id}/finalizar`, datos);
  }

  editar(id: number, manga: Partial<Manga>): Observable<Manga> {
    return this.http.put<Manga>(`${this.url}/${id}`, manga);
  }

  marcarComoPendiente(id: number): Observable<void> {
    return this.http.put<void>(`${this.url}/${id}/pendiente`, {});
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  subirImagen(file: File): Observable<{ url: string }> {
    const formData = new FormData();
    formData.append('imagen', file);
    return this.http.post<{ url: string }>('http://localhost:8090/api/imagenes/subir', formData);
  }
}

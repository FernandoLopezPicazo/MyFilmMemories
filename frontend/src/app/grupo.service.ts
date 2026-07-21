import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { Amigo } from './amigo.service';

export interface Grupo {
  id: number;
  nombre: string;
  fechaCreacion: string;
  numeroMiembros: number;
}

export interface GrupoDetalle {
  id: number;
  nombre: string;
  fechaCreacion: string;
  miembros: Amigo[];
}

export interface GrupoInvitacion {
  id: number;
  grupoId: number;
  nombreGrupo: string;
  deEmail: string;
  fechaInvitacion: string;
}

export type TipoGrupoItem = 'SERIE' | 'PELICULA' | 'MANGA';

export interface OpinionGrupo {
  usuarioId: string;
  email: string;
  nota?: number;
  personajeFavorito?: string;
  personajeOdiado?: string;
  comentario?: string;
}

export interface GrupoItem {
  id: number;
  tipo: TipoGrupoItem;
  titulo: string;
  descripcion?: string;
  imagenUrl?: string;
  generos: string[];
  opiniones: OpinionGrupo[];
}

@Injectable({ providedIn: 'root' })
export class GrupoService {

  private url = `${environment.apiUrl}/api/grupos`;

  constructor(private http: HttpClient) {}

  crear(nombre: string): Observable<Grupo> {
    return this.http.post<Grupo>(this.url, { nombre });
  }

  misGrupos(): Observable<Grupo[]> {
    return this.http.get<Grupo[]>(this.url);
  }

  detalle(grupoId: number): Observable<GrupoDetalle> {
    return this.http.get<GrupoDetalle>(`${this.url}/${grupoId}`);
  }

  invitar(grupoId: number, email: string): Observable<void> {
    return this.http.post<void>(`${this.url}/${grupoId}/invitaciones`, { email });
  }

  invitacionesRecibidas(): Observable<GrupoInvitacion[]> {
    return this.http.get<GrupoInvitacion[]>(`${this.url}/invitaciones/recibidas`);
  }

  aceptarInvitacion(invitacionId: number): Observable<void> {
    return this.http.put<void>(`${this.url}/invitaciones/${invitacionId}/aceptar`, {});
  }

  rechazarInvitacion(invitacionId: number): Observable<void> {
    return this.http.put<void>(`${this.url}/invitaciones/${invitacionId}/rechazar`, {});
  }

  salir(grupoId: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${grupoId}/salir`);
  }

  listarItems(grupoId: number, tipo: TipoGrupoItem): Observable<GrupoItem[]> {
    return this.http.get<GrupoItem[]>(`${this.url}/${grupoId}/items?tipo=${tipo}`);
  }

  crearItem(grupoId: number, tipo: TipoGrupoItem, titulo: string, descripcion: string,
            generos: string[], imagenUrl?: string | null): Observable<GrupoItem> {
    return this.http.post<GrupoItem>(`${this.url}/${grupoId}/items`, { tipo, titulo, descripcion, generos, imagenUrl });
  }

  eliminarItem(grupoId: number, itemId: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${grupoId}/items/${itemId}`);
  }

  opinar(grupoId: number, itemId: number, nota: number | null, personajeFavorito: string,
         personajeOdiado: string, comentario: string): Observable<void> {
    return this.http.put<void>(`${this.url}/${grupoId}/items/${itemId}/opinion`,
        { nota, personajeFavorito, personajeOdiado, comentario });
  }
}

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface ItemProgramado {
  id: number;
  tipo: 'SERIE' | 'PELICULA' | 'MANGA';
  titulo: string;
  imagenUrl: string | null;
  diaSemana: 'LUNES' | 'MARTES' | 'MIERCOLES' | 'JUEVES' | 'VIERNES' | 'SABADO' | 'DOMINGO';
  frecuencia: 'SEMANAL' | 'MENSUAL' | null;
  semanaDelMes: number | null;
}

@Injectable({ providedIn: 'root' })
export class HorarioService {

  private url = `${environment.apiUrl}/api/horario`;

  constructor(private http: HttpClient) {}

  obtenerHorario(): Observable<ItemProgramado[]> {
    return this.http.get<ItemProgramado[]>(this.url);
  }
}

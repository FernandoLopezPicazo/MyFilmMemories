import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject } from 'rxjs';
import { environment } from '../environments/environment';

export type EstadoBackend = 'comprobando' | 'listo' | 'error';

/*
 * El backend gratuito de Render se duerme tras 15 min de inactividad y tarda
 * ~30-60s en despertar. Este servicio hace ping a GET /health (sin auth) al
 * arrancar la app y reintenta cada pocos segundos hasta que responde, para
 * poder mostrar un aviso en vez de que el usuario piense que la app está rota.
 */
@Injectable({ providedIn: 'root' })
export class BackendStatusService {

  private estadoSubject = new BehaviorSubject<EstadoBackend>('comprobando');
  estado$ = this.estadoSubject.asObservable();

  constructor(private http: HttpClient) {
    this.comprobar();
  }

  private comprobar(): void {
    this.http.get(`${environment.apiUrl}/health`).subscribe({
      next: () => this.estadoSubject.next('listo'),
      error: () => {
        this.estadoSubject.next('error');
        setTimeout(() => this.comprobar(), 5000);
      }
    });
  }
}

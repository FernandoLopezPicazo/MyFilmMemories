import { Injectable } from '@angular/core';

export interface ColorScheme {
  nombre: string;
  pendiente: string;
  proceso: string;
  finalizado: string;
  acento: string;
  // Variantes oscuras del mismo matiz, usadas cuando el modo oscuro está activo
  pendienteOscuro: string;
  procesoOscuro: string;
  finalizadoOscuro: string;
  acentoOscuro: string;
}

export const ESQUEMAS: ColorScheme[] = [
  { nombre: 'Índigo',
    pendiente: '#e0e7ff', proceso: '#dbeafe', finalizado: '#dcfce7', acento: '#4f46e5',
    pendienteOscuro: '#1e3a5f', procesoOscuro: '#1a3a4a', finalizadoOscuro: '#14352a', acentoOscuro: '#6366f1' },
  { nombre: 'Esmeralda',
    pendiente: '#d1fae5', proceso: '#cffafe', finalizado: '#e0f2fe', acento: '#059669',
    pendienteOscuro: '#0f3d2e', procesoOscuro: '#0e3a3d', finalizadoOscuro: '#0c2d42', acentoOscuro: '#10b981' },
  { nombre: 'Rosa',
    pendiente: '#fce7f3', proceso: '#fef3c7', finalizado: '#dcfce7', acento: '#db2777',
    pendienteOscuro: '#4a1942', procesoOscuro: '#4a3a12', finalizadoOscuro: '#14352a', acentoOscuro: '#ec4899' },
  { nombre: 'Ámbar',
    pendiente: '#fef9c3', proceso: '#fed7aa', finalizado: '#d1fae5', acento: '#d97706',
    pendienteOscuro: '#4a3f0a', procesoOscuro: '#4a2a0a', finalizadoOscuro: '#0f3d2e', acentoOscuro: '#f59e0b' },
  { nombre: 'Pizarra',
    pendiente: '#f1f5f9', proceso: '#e2e8f0', finalizado: '#cbd5e1', acento: '#475569',
    pendienteOscuro: '#1e293b', procesoOscuro: '#293548', finalizadoOscuro: '#334155', acentoOscuro: '#94a3b8' },
  { nombre: 'Orquídea',
    pendiente: '#ffe4f3', proceso: '#f0e6ff', finalizado: '#ffd9f7', acento: '#e454c9',
    pendienteOscuro: '#4a1f42', procesoOscuro: '#3d2159', finalizadoOscuro: '#4a1a45', acentoOscuro: '#f0abfc' },
];

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private _dark = false;
  private _esquema: ColorScheme = ESQUEMAS[0];

  get dark() { return this._dark; }
  get esquema() { return this._esquema; }

  constructor() {
    const savedDark = localStorage.getItem('dark') === 'true';
    const savedEsquema = localStorage.getItem('esquema');
    const esquema = savedEsquema ? ESQUEMAS.find(e => e.nombre === savedEsquema) : undefined;
    if (esquema) this._esquema = esquema;
    this.applyDark(savedDark);
    this.applyScheme(this._esquema);
  }

  toggleDark(): void {
    this.applyDark(!this._dark);
    this.applyScheme(this._esquema);
    localStorage.setItem('dark', String(this._dark));
  }

  setEsquema(e: ColorScheme): void {
    this._esquema = e;
    this.applyScheme(e);
    localStorage.setItem('esquema', e.nombre);
  }

  private applyDark(dark: boolean): void {
    this._dark = dark;
    if (dark) document.documentElement.classList.add('dark');
    else document.documentElement.classList.remove('dark');
  }

  private applyScheme(e: ColorScheme): void {
    const r = document.documentElement.style;
    if (this._dark) {
      r.setProperty('--color-pendiente', e.pendienteOscuro);
      r.setProperty('--color-proceso', e.procesoOscuro);
      r.setProperty('--color-finalizado', e.finalizadoOscuro);
      r.setProperty('--acento', e.acentoOscuro);
    } else {
      r.setProperty('--color-pendiente', e.pendiente);
      r.setProperty('--color-proceso', e.proceso);
      r.setProperty('--color-finalizado', e.finalizado);
      r.setProperty('--acento', e.acento);
    }
  }
}

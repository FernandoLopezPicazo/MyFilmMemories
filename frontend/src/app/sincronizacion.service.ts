import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../environments/environment';
import { AuthService } from './auth.service';
import { SerieService } from './serie.service';
import { PeliculaService } from './pelicula.service';
import { MangaService } from './manga.service';

interface ItemSincronizado<T> {
  localId: number | null;
  item: T;
}

export interface ResultadoSincronizacion {
  series: number;
  peliculas: number;
  mangas: number;
}

/*
 * Orquesta la sincronización escritorio↔nube en dos pasos por tipo de
 * título: 1) manda el lote local al backend de LA NUBE (con tu sesión),
 * que fusiona y devuelve el estado combinado; 2) manda ese mismo resultado
 * al backend LOCAL (sin login), que al tener ya todo con syncId simplemente
 * actualiza lo que haya cambiado — el mismo endpoint sirve para los dos
 * pasos porque la lógica de fusión no depende de dónde se ejecute
 * (ver SincronizacionService en el backend).
 */
@Injectable({ providedIn: 'root' })
export class SincronizacionService {

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private serieService: SerieService,
    private peliculaService: PeliculaService,
    private mangaService: MangaService
  ) {}

  async sincronizar(): Promise<ResultadoSincronizacion> {
    const token = this.auth.getAccessToken();
    if (!token) throw new Error('Inicia sesión para sincronizar con la nube');

    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });

    const [series, peliculas, mangas] = await Promise.all([
      firstValueFrom(this.serieService.obtenerTodas()),
      firstValueFrom(this.peliculaService.obtenerTodas()),
      firstValueFrom(this.mangaService.obtenerTodos())
    ]);

    const seriesResultado = await this.sincronizarTipo('series', series, headers);
    const peliculasResultado = await this.sincronizarTipo('peliculas', peliculas, headers);
    const mangasResultado = await this.sincronizarTipo('mangas', mangas, headers);

    return { series: seriesResultado, peliculas: peliculasResultado, mangas: mangasResultado };
  }

  private async sincronizarTipo(
    tipo: 'series' | 'peliculas' | 'mangas',
    lote: any[],
    headers: HttpHeaders
  ): Promise<number> {
    // Paso 1: fusionar contra la nube (con sesión)
    const fusionado = await firstValueFrom(
      this.http.post<ItemSincronizado<any>[]>(`${environment.nubeApiUrl}/api/sincronizacion/${tipo}`, lote, { headers })
    );

    // Paso 2: aplicar el resultado fusionado en el backend local (sin login)
    const items = fusionado.map(r => r.item);
    await firstValueFrom(
      this.http.post<ItemSincronizado<any>[]>(`${environment.apiUrl}/api/sincronizacion/${tipo}`, items)
    );

    return items.length;
  }
}

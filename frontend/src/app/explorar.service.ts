import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map, catchError } from 'rxjs';
import { environment } from '../environments/environment';

export interface ResultadoExplorar {
  id: number;
  titulo: string;
  descripcion: string;
  imagenUrl: string | null;
  puntuacion: number;
  generos: string[];
  tipo: 'pelicula' | 'serie' | 'manga';
  fuente: 'tmdb' | 'jikan' | 'anilist';
}

@Injectable({ providedIn: 'root' })
export class ExplorarService {

  private readonly tmdbUrl = environment.tmdbBaseUrl;
  private readonly apiKey = environment.tmdbApiKey;
  private readonly imgUrl = environment.tmdbImageUrl;
  private readonly jikanUrl = 'https://api.jikan.moe/v4';
  // Colchón cuando Jikan falla (caídas/rate-limit de MyAnimeList): AniList
  // es otra API gratuita de anime/manga, sin key, más estable que Jikan.
  private readonly anilistUrl = 'https://graphql.anilist.co';

  // Mapas de id de género TMDB → nombre
  private readonly generosPelicula: Record<number, string> = {
    28:'Acción',12:'Aventura',16:'Animación',35:'Comedia',80:'Crimen',
    99:'Documental',18:'Drama',10751:'Familia',14:'Fantasía',36:'Historia',
    27:'Terror',10402:'Música',9648:'Misterio',10749:'Romance',
    878:'Ciencia Ficción',10770:'Película de TV',53:'Suspenso',
    10752:'Bélica',37:'Western'
  };
  private readonly generosSerie: Record<number, string> = {
    10759:'Acción y aventura',16:'Animación',35:'Comedia',80:'Crimen',
    99:'Documental',18:'Drama',10751:'Familia',10762:'Infantil',
    9648:'Misterio',10763:'Noticias',10764:'Reality',
    10765:'Ciencia ficción y fantasía',10766:'Telenovela',10767:'Talk Show',
    10768:'Guerra y política',37:'Western'
  };

  // Mapas inversos nombre → ID para las llamadas discover
  private readonly generosPeliculaId: Record<string, number> = Object.fromEntries(
    Object.entries(this.generosPelicula).map(([id, name]) => [name, +id])
  );
  private readonly generosSerieId: Record<string, number> = Object.fromEntries(
    Object.entries(this.generosSerie).map(([id, name]) => [name, +id])
  );
  // Jikan: nombre español → ID de MyAnimeList
  private readonly generosJikanId: Record<string, number> = {
    'Acción': 1, 'Aventura': 2, 'Comedia': 4, 'Drama': 8, 'Fantasía': 10,
    'Horror': 14, 'Misterio': 7, 'Romance': 22, 'Ciencia Ficción': 24, 'Deportes': 30,
    'Slice of Life': 36, 'Sobrenatural': 37, 'Militar': 38, 'Psicológico': 40,
    'Seinen': 41, 'Shoujo': 42, 'Shounen': 43, 'Josei': 44, 'Mecha': 18,
    'Ecchi': 9, 'Harem': 35, 'Isekai': 62, 'Música': 19, 'Artes Marciales': 17,
    'Thriller': 40
  };
  // Traducción inglés → español para géneros que devuelve Jikan
  private readonly jikanTraduccion: Record<string, string> = {
    'Action': 'Acción', 'Adventure': 'Aventura', 'Comedy': 'Comedia', 'Drama': 'Drama',
    'Fantasy': 'Fantasía', 'Horror': 'Horror', 'Mystery': 'Misterio', 'Romance': 'Romance',
    'Sci-Fi': 'Ciencia Ficción', 'Sports': 'Deportes', 'Slice of Life': 'Slice of Life',
    'Supernatural': 'Sobrenatural', 'Military': 'Militar', 'Psychological': 'Psicológico',
    'Seinen': 'Seinen', 'Shoujo': 'Shoujo', 'Shounen': 'Shounen', 'Josei': 'Josei',
    'Mecha': 'Mecha', 'Ecchi': 'Ecchi', 'Harem': 'Harem', 'Isekai': 'Isekai',
    'Music': 'Música', 'Martial Arts': 'Artes Marciales', 'Thriller': 'Thriller',
    'School': 'Escolar', 'Demons': 'Demonios', 'Magic': 'Magia', 'Historical': 'Histórico',
    'Kids': 'Infantil', 'Parody': 'Parodia', 'Samurai': 'Samurái', 'Space': 'Espacial',
    'Vampire': 'Vampiros', 'Yaoi': 'Yaoi', 'Yuri': 'Yuri'
  };

  constructor(private http: HttpClient) {}

  // ── PELÍCULAS ──────────────────────────────────────
  trendingPeliculas(page = 1): Observable<ResultadoExplorar[]> {
    return this.http.get<any>(`${this.tmdbUrl}/trending/movie/week`, {
      params: new HttpParams().set('api_key', this.apiKey).set('language', 'es-ES').set('page', page)
    }).pipe(map(r => r.results.map((i: any) => this.mapPelicula(i))));
  }

  buscarPeliculas(query: string, page = 1): Observable<ResultadoExplorar[]> {
    return this.http.get<any>(`${this.tmdbUrl}/search/movie`, {
      params: new HttpParams().set('api_key', this.apiKey).set('language', 'es-ES').set('query', query).set('page', page)
    }).pipe(map(r => r.results.map((i: any) => this.mapPelicula(i))));
  }

  descubrirPeliculas(generos: string[], page = 1): Observable<ResultadoExplorar[]> {
    const ids = generos.map(g => this.generosPeliculaId[g]).filter(Boolean).join(',');
    if (!ids) return this.trendingPeliculas(page);
    return this.http.get<any>(`${this.tmdbUrl}/discover/movie`, {
      params: new HttpParams().set('api_key', this.apiKey).set('language', 'es-ES')
        .set('with_genres', ids).set('sort_by', 'popularity.desc').set('page', page)
    }).pipe(map(r => r.results.map((i: any) => this.mapPelicula(i))));
  }

  private truncar(texto: string, max = 4000): string {
    return texto && texto.length > max ? texto.slice(0, max) + '…' : texto || '';
  }

  private mapPelicula(i: any): ResultadoExplorar {
    return {
      id: i.id, tipo: 'pelicula', fuente: 'tmdb',
      titulo: i.title || i.original_title,
      descripcion: this.truncar(i.overview),
      imagenUrl: i.poster_path ? this.imgUrl + i.poster_path : null,
      puntuacion: Math.round(i.vote_average * 10) / 10,
      generos: (i.genre_ids || []).map((id: number) => this.generosPelicula[id]).filter(Boolean)
    };
  }

  // ── SERIES ─────────────────────────────────────────
  trendingSeries(page = 1): Observable<ResultadoExplorar[]> {
    return this.http.get<any>(`${this.tmdbUrl}/trending/tv/week`, {
      params: new HttpParams().set('api_key', this.apiKey).set('language', 'es-ES').set('page', page)
    }).pipe(map(r => r.results.map((i: any) => this.mapSerie(i))));
  }

  buscarSeries(query: string, page = 1): Observable<ResultadoExplorar[]> {
    return this.http.get<any>(`${this.tmdbUrl}/search/tv`, {
      params: new HttpParams().set('api_key', this.apiKey).set('language', 'es-ES').set('query', query).set('page', page)
    }).pipe(map(r => r.results.map((i: any) => this.mapSerie(i))));
  }

  descubrirSeries(generos: string[], page = 1): Observable<ResultadoExplorar[]> {
    const ids = generos.map(g => this.generosSerieId[g]).filter(Boolean).join(',');
    if (!ids) return this.trendingSeries(page);
    return this.http.get<any>(`${this.tmdbUrl}/discover/tv`, {
      params: new HttpParams().set('api_key', this.apiKey).set('language', 'es-ES')
        .set('with_genres', ids).set('sort_by', 'popularity.desc').set('page', page)
    }).pipe(map(r => r.results.map((i: any) => this.mapSerie(i))));
  }

  private mapSerie(i: any): ResultadoExplorar {
    return {
      id: i.id, tipo: 'serie', fuente: 'tmdb',
      titulo: i.name || i.original_name,
      descripcion: this.truncar(i.overview),
      imagenUrl: i.poster_path ? this.imgUrl + i.poster_path : null,
      puntuacion: Math.round(i.vote_average * 10) / 10,
      generos: (i.genre_ids || []).map((id: number) => this.generosSerie[id]).filter(Boolean)
    };
  }

  // ── MANGAS (Jikan / MyAnimeList, con AniList como colchón) ──
  topMangas(page = 1): Observable<ResultadoExplorar[]> {
    return this.http.get<any>(`${this.jikanUrl}/top/manga`, {
      params: new HttpParams().set('limit', '20').set('page', page)
    }).pipe(
      map(r => r.data.map((i: any) => this.mapManga(i))),
      catchError(() => this.topMangasAniList(page))
    );
  }

  buscarMangas(query: string, page = 1): Observable<ResultadoExplorar[]> {
    return this.http.get<any>(`${this.jikanUrl}/manga`, {
      params: new HttpParams().set('q', query).set('limit', '20').set('page', page)
    }).pipe(
      map(r => r.data.map((i: any) => this.mapManga(i))),
      catchError(() => this.buscarMangasAniList(query, page))
    );
  }

  descubrirMangas(generos: string[], page = 1): Observable<ResultadoExplorar[]> {
    const ids = generos.map(g => this.generosJikanId[g]).filter(Boolean).join(',');
    if (!ids) return this.topMangas(page);
    return this.http.get<any>(`${this.jikanUrl}/manga`, {
      params: new HttpParams().set('genres', ids).set('limit', '24').set('order_by', 'popularity').set('page', page)
    }).pipe(
      map(r => r.data.map((i: any) => this.mapManga(i))),
      catchError(() => this.descubrirMangasAniList(generos, page))
    );
  }

  private mapManga(i: any): ResultadoExplorar {
    return {
      id: i.mal_id, tipo: 'manga', fuente: 'jikan',
      titulo: i.title_spanish || i.title,
      descripcion: this.truncar(i.synopsis),
      imagenUrl: i.images?.jpg?.image_url || null,
      puntuacion: i.score || 0,
      generos: (i.genres || []).map((g: any) => this.jikanTraduccion[g.name] || g.name)
    };
  }

  // ── MANGAS — AniList (colchón si Jikan falla) ──────
  // AniList devuelve/acepta géneros en inglés, igual que Jikan — reutilizamos
  // el mismo diccionario de traducción invertido en vez de mantener otro.
  private readonly espanolAIngles: Record<string, string> = Object.fromEntries(
    Object.entries(this.jikanTraduccion).map(([en, es]) => [es, en])
  );

  private readonly camposMediaAniList = `
    id
    title { romaji english }
    description(asHtml: false)
    coverImage { large }
    averageScore
    genres
  `;

  private consultarAniList(query: string, variables: Record<string, unknown>): Observable<ResultadoExplorar[]> {
    return this.http.post<any>(this.anilistUrl, { query, variables })
      .pipe(map(r => r.data.Page.media.map((i: any) => this.mapMangaAniList(i))));
  }

  private topMangasAniList(page = 1): Observable<ResultadoExplorar[]> {
    const query = `query ($page: Int) {
      Page(page: $page, perPage: 20) {
        media(type: MANGA, sort: POPULARITY_DESC) { ${this.camposMediaAniList} }
      }
    }`;
    return this.consultarAniList(query, { page });
  }

  private buscarMangasAniList(q: string, page = 1): Observable<ResultadoExplorar[]> {
    const query = `query ($q: String, $page: Int) {
      Page(page: $page, perPage: 20) {
        media(type: MANGA, search: $q) { ${this.camposMediaAniList} }
      }
    }`;
    return this.consultarAniList(query, { q, page });
  }

  private descubrirMangasAniList(generos: string[], page = 1): Observable<ResultadoExplorar[]> {
    const ingles = generos.map(g => this.espanolAIngles[g]).filter(Boolean);
    if (!ingles.length) return this.topMangasAniList(page);
    const query = `query ($generos: [String], $page: Int) {
      Page(page: $page, perPage: 24) {
        media(type: MANGA, genre_in: $generos, sort: POPULARITY_DESC) { ${this.camposMediaAniList} }
      }
    }`;
    return this.consultarAniList(query, { generos: ingles, page });
  }

  private mapMangaAniList(i: any): ResultadoExplorar {
    return {
      id: i.id, tipo: 'manga', fuente: 'anilist',
      titulo: i.title?.english || i.title?.romaji,
      descripcion: this.truncar((i.description || '').replace(/<[^>]+>/g, '')),
      imagenUrl: i.coverImage?.large || null,
      puntuacion: i.averageScore ? Math.round(i.averageScore) / 10 : 0,
      generos: (i.genres || []).map((g: string) => this.jikanTraduccion[g] || g)
    };
  }
}

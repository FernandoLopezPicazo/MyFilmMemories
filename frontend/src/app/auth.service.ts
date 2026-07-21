import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { createClient, Session, SupabaseClient } from '@supabase/supabase-js';
import { BehaviorSubject } from 'rxjs';
import { environment } from '../environments/environment';

/*
 * Gestiona login/registro/sesión con Supabase Auth. Si el environment no
 * tiene credenciales de Supabase configuradas (como en dev/Electron, donde
 * el backend local no exige autenticación — ver SecurityConfigDev en el
 * backend), este servicio queda "deshabilitado": no crea ningún cliente,
 * nunca hay sesión, y AuthGuard deja pasar todas las rutas sin pedir login,
 * preservando el comportamiento actual del instalador de escritorio.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {

  private supabase: SupabaseClient | null = null;
  private sessionSubject = new BehaviorSubject<Session | null>(null);
  session$ = this.sessionSubject.asObservable();

  private inicializado: Promise<void>;

  constructor(private http: HttpClient) {
    if (environment.supabaseUrl && environment.supabaseAnonKey) {
      this.supabase = createClient(environment.supabaseUrl, environment.supabaseAnonKey);

      this.inicializado = this.supabase.auth.getSession().then(({ data }) => {
        this.sessionSubject.next(data.session);
        if (data.session) this.sincronizarPerfil();
      });

      this.supabase.auth.onAuthStateChange((_evento, session) => {
        this.sessionSubject.next(session);
      });
    } else {
      this.inicializado = Promise.resolve();
    }
  }

  // El backend necesita saber qué email corresponde a tu usuario_id para que
  // el sistema de amigos pueda buscarte — ver PerfilController en el backend.
  private sincronizarPerfil(): void {
    this.http.post(`${environment.apiUrl}/api/perfiles/sincronizar`, {}).subscribe({ error: () => {} });
  }

  // false en dev/Electron (sin credenciales de Supabase) — true en la nube
  get habilitado(): boolean {
    return this.supabase !== null;
  }

  get haySesion(): boolean {
    return this.sessionSubject.value !== null;
  }

  get email(): string | null {
    return this.sessionSubject.value?.user.email ?? null;
  }

  esperarInicializacion(): Promise<void> {
    return this.inicializado;
  }

  getAccessToken(): string | null {
    return this.sessionSubject.value?.access_token ?? null;
  }

  async signUp(email: string, password: string) {
    if (!this.supabase) throw new Error('Supabase no está configurado en este entorno');
    const { error } = await this.supabase.auth.signUp({ email, password });
    if (error) throw error;
  }

  async signIn(email: string, password: string) {
    if (!this.supabase) throw new Error('Supabase no está configurado en este entorno');
    const { error } = await this.supabase.auth.signInWithPassword({ email, password });
    if (error) throw error;
    this.sincronizarPerfil();
  }

  async signOut() {
    if (!this.supabase) return;
    await this.supabase.auth.signOut();
  }
}

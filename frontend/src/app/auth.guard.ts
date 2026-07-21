import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from './auth.service';

/*
 * Protege las rutas de la app. Se rige por requiereLogin (no por
 * "habilitado"): en dev/Electron deja pasar siempre sin pedir login, aunque
 * Supabase esté configurado (el escritorio ofrece login opcional solo para
 * sincronizar). En la nube exige sesión activa, redirigiendo a /login.
 */
@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  async canActivate(): Promise<boolean> {
    if (!this.authService.requiereLogin) return true;

    await this.authService.esperarInicializacion();

    if (this.authService.haySesion) return true;

    this.router.navigate(['/login']);
    return false;
  }
}

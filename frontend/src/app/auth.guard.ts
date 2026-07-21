import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from './auth.service';

/*
 * Protege las rutas de la app. En dev/Electron (AuthService.habilitado ===
 * false) deja pasar siempre, sin pedir login — igual que hoy. En la nube
 * (con Supabase configurado) exige una sesión activa, redirigiendo a
 * /login si no la hay.
 */
@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  async canActivate(): Promise<boolean> {
    if (!this.authService.habilitado) return true;

    await this.authService.esperarInicializacion();

    if (this.authService.haySesion) return true;

    this.router.navigate(['/login']);
    return false;
  }
}

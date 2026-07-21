import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { environment } from '../environments/environment';

/*
 * Añade "Authorization: Bearer <token>" solo a las peticiones que van hacia
 * nuestro propio backend (environment.apiUrl) — nunca a las llamadas a la
 * API de TMDB/Jikan (explorar.service.ts), que no deben llevar este token.
 * En dev/Electron no hay token (AuthService deshabilitado), así que esta
 * clase no añade ninguna cabecera y las peticiones salen exactamente igual
 * que antes de esta migración.
 */
@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getAccessToken();

    if (token && req.url.startsWith(environment.apiUrl)) {
      req = req.clone({
        setHeaders: { Authorization: `Bearer ${token}` }
      });
    }

    return next.handle(req);
  }
}

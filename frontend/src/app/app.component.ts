import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ThemeService, ESQUEMAS, ColorScheme } from './theme.service';
import { AuthService } from './auth.service';
import { BackendStatusService } from './backend-status.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  readonly esquemas = ESQUEMAS;
  mostrarPaleta = false;
  mostrarMenuUsuario = false;

  constructor(
    public theme: ThemeService,
    public auth: AuthService,
    public backend: BackendStatusService,
    private router: Router
  ) {}

  seleccionarEsquema(e: ColorScheme): void {
    this.theme.setEsquema(e);
    this.mostrarPaleta = false;
  }

  async cerrarSesion(): Promise<void> {
    await this.auth.signOut();
    this.mostrarMenuUsuario = false;
    this.router.navigate(['/login']);
  }
}

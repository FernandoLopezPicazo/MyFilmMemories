import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {

  modo: 'entrar' | 'registrar' = 'entrar';
  email = '';
  password = '';
  cargando = false;
  error = '';

  constructor(private authService: AuthService, private router: Router) {}

  cambiarModo(): void {
    this.modo = this.modo === 'entrar' ? 'registrar' : 'entrar';
    this.error = '';
  }

  async enviar(): Promise<void> {
    if (!this.email.trim() || !this.password.trim()) {
      this.error = 'Rellena email y contraseña.';
      return;
    }

    this.cargando = true;
    this.error = '';

    try {
      if (this.modo === 'entrar') {
        await this.authService.signIn(this.email.trim(), this.password);
        this.router.navigate(['/series']);
      } else {
        await this.authService.signUp(this.email.trim(), this.password);
        this.error = '';
        this.modo = 'entrar';
        this.password = '';
        // Supabase puede exigir confirmación por email según la config del proyecto
        alert('Cuenta creada. Si tu proyecto de Supabase exige confirmación, revisa tu correo antes de entrar.');
      }
    } catch (e: any) {
      this.error = e?.message || 'Ha ocurrido un error. Inténtalo de nuevo.';
    } finally {
      this.cargando = false;
    }
  }
}

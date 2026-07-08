import { Component } from '@angular/core';
import { ThemeService, ESQUEMAS, ColorScheme } from './theme.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  readonly esquemas = ESQUEMAS;
  mostrarPaleta = false;

  constructor(public theme: ThemeService) {}

  seleccionarEsquema(e: ColorScheme): void {
    this.theme.setEsquema(e);
    this.mostrarPaleta = false;
  }
}

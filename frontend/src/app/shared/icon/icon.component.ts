import { Component, Input } from '@angular/core';

/*
 * Set de iconos originales en SVG, dibujados a mano para MyFilmMemories.
 * Usan currentColor para heredar el color del texto del elemento padre,
 * así se adaptan automáticamente al modo claro/oscuro sin CSS extra.
 */
@Component({
  selector: 'app-icon',
  template: `
    <svg [attr.width]="size" [attr.height]="size" viewBox="0 0 24 24"
         fill="none" stroke="currentColor" stroke-width="1.7"
         stroke-linecap="round" stroke-linejoin="round" class="app-icon">

      <!-- Logo: marco de visor con un corazón — "recuerdos enmarcados" -->
      <ng-container *ngIf="name === 'brand'">
        <path d="M3 8V4h4" />
        <path d="M21 8V4h-4" />
        <path d="M3 16v4h4" />
        <path d="M21 16v4h-4" />
        <path d="M12 16.8c-3.6-2.2-5.4-4.3-5.4-6.6a3 3 0 0 1 5.4-1.8 3 3 0 0 1 5.4 1.8c0 2.3-1.8 4.4-5.4 6.6Z"
              [attr.fill]="'currentColor'" fill-opacity="0.18" stroke-width="1.5" />
      </ng-container>

      <!-- Series: monitor con triángulo de play -->
      <ng-container *ngIf="name === 'series'">
        <rect x="3" y="4.5" width="18" height="12" rx="2" />
        <path d="M9 16.5v2M15 16.5v2M7.5 20.5h9" />
        <path d="M10.3 8.2v4.6l3.9-2.3-3.9-2.3Z" [attr.fill]="'currentColor'" stroke="none" />
      </ng-container>

      <!-- Películas: claqueta de cine -->
      <ng-container *ngIf="name === 'peliculas'">
        <path d="M4 10.5h16V19a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1v-8.5Z" />
        <path d="M4 10.5 5 5.5h4.2l-1.6 5" />
        <path d="M11.2 10.5l1.6-5h4.2l-1.6 5" />
        <path d="M4.6 7.7 19 7.9" />
      </ng-container>

      <!-- Mangas: libro abierto -->
      <ng-container *ngIf="name === 'mangas'">
        <path d="M12 6.2c-1.6-1.1-3.8-1.7-6-1.7-1 0-2 .1-3 .4v13c1-.3 2-.4 3-.4 2.2 0 4.4.6 6 1.7" />
        <path d="M12 6.2c1.6-1.1 3.8-1.7 6-1.7 1 0 2 .1 3 .4v13c-1-.3-2-.4-3-.4-2.2 0-4.4.6-6 1.7" />
        <path d="M12 6.2v13.7" />
      </ng-container>

      <!-- Compartir: dos flechas en bucle (intercambio import/export) -->
      <ng-container *ngIf="name === 'compartir'">
        <path d="M4 8.5h11.5a3.5 3.5 0 0 1 3.5 3.5v1" />
        <path d="M13 5.3 16.2 8.5 13 11.7" />
        <path d="M20 15.5H8.5A3.5 3.5 0 0 1 5 12v-1" />
        <path d="M11 18.7 7.8 15.5 11 12.3" />
      </ng-container>

      <!-- Amigos: dos personas -->
      <ng-container *ngIf="name === 'amigos'">
        <circle cx="8.5" cy="8" r="2.8" />
        <path d="M3.5 19c0-3 2.2-5 5-5s5 2 5 5" />
        <circle cx="16.5" cy="8.5" r="2.2" />
        <path d="M14.8 14.3c2.2.3 3.7 2.1 3.7 4.7" />
      </ng-container>

      <!-- Grupos: tres personas -->
      <ng-container *ngIf="name === 'grupos'">
        <circle cx="12" cy="7.5" r="2.6" />
        <path d="M6.5 19c0-3 2.5-5.2 5.5-5.2s5.5 2.2 5.5 5.2" />
        <circle cx="4.8" cy="9.5" r="1.8" />
        <path d="M2 17.5c0-2.1 1.3-3.6 2.8-3.9" />
        <circle cx="19.2" cy="9.5" r="1.8" />
        <path d="M22 17.5c0-2.1-1.3-3.6-2.8-3.9" />
      </ng-container>

      <!-- Paleta de colores -->
      <ng-container *ngIf="name === 'paleta'">
        <path d="M12 3.5c-4.8 0-8.5 3.6-8.5 8 0 3 2 4.7 4 4.7.9 0 1.4-.5 1.4-1.2 0-.6-.4-.9-.4-1.6 0-.9.8-1.6 1.8-1.6h2.3c2.7 0 5.4-1.9 5.4-5.3 0-1.6-2.6-3-6-3Z" />
        <circle cx="8.3" cy="10.2" r=".9" [attr.fill]="'currentColor'" stroke="none" />
        <circle cx="11.6" cy="7.6" r=".9" [attr.fill]="'currentColor'" stroke="none" />
        <circle cx="15.2" cy="8.6" r=".9" [attr.fill]="'currentColor'" stroke="none" />
      </ng-container>

      <!-- Sol (modo claro) -->
      <ng-container *ngIf="name === 'sol'">
        <circle cx="12" cy="12" r="4" />
        <path d="M12 2.5v2M12 19.5v2M4.2 4.2l1.4 1.4M18.4 18.4l1.4 1.4M2.5 12h2M19.5 12h2M4.2 19.8l1.4-1.4M18.4 5.6l1.4-1.4" />
      </ng-container>

      <!-- Luna (modo oscuro) -->
      <ng-container *ngIf="name === 'luna'">
        <path d="M20 14.2A8.5 8.5 0 1 1 9.8 4a6.8 6.8 0 0 0 10.2 10.2Z" />
      </ng-container>

    </svg>
  `,
  styles: [`
    :host { display: inline-flex; align-items: center; justify-content: center; line-height: 0; }
    .app-icon { display: block; }
  `]
})
export class IconComponent {
  @Input() name = '';
  @Input() size = 20;
}

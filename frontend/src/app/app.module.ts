import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

import { AppComponent } from './app.component';
import { SeriesPageComponent } from './series-page/series-page.component';
import { PeliculasPageComponent } from './peliculas-page/peliculas-page.component';
import { MangasPageComponent } from './mangas-page/mangas-page.component';
import { BackupPageComponent } from './backup-page/backup-page.component';
import { LoginComponent } from './login/login.component';
import { AmigosPageComponent } from './amigos-page/amigos-page.component';
import { GruposPageComponent } from './grupos-page/grupos-page.component';
import { HorarioPageComponent } from './horario-page/horario-page.component';
import { IconComponent } from './shared/icon/icon.component';
import { AppRoutingModule } from './app-routing.module';
import { AuthInterceptor } from './auth.interceptor';

@NgModule({
  declarations: [
    AppComponent, SeriesPageComponent, PeliculasPageComponent, MangasPageComponent,
    BackupPageComponent, LoginComponent, AmigosPageComponent, GruposPageComponent,
    HorarioPageComponent, IconComponent
  ],
  imports: [
    BrowserModule,
    CommonModule,
    HttpClientModule,
    FormsModule,
    AppRoutingModule
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}

import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

import { AppComponent } from './app.component';
import { SeriesPageComponent } from './series-page/series-page.component';
import { PeliculasPageComponent } from './peliculas-page/peliculas-page.component';
import { MangasPageComponent } from './mangas-page/mangas-page.component';
import { BackupPageComponent } from './backup-page/backup-page.component';
import { IconComponent } from './shared/icon/icon.component';
import { AppRoutingModule } from './app-routing.module';

@NgModule({
  declarations: [AppComponent, SeriesPageComponent, PeliculasPageComponent, MangasPageComponent, BackupPageComponent, IconComponent],
  imports: [
    BrowserModule,
    CommonModule,
    HttpClientModule,
    FormsModule,
    AppRoutingModule
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}

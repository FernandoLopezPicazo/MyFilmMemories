import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SeriesPageComponent } from './series-page/series-page.component';
import { PeliculasPageComponent } from './peliculas-page/peliculas-page.component';
import { MangasPageComponent } from './mangas-page/mangas-page.component';
import { BackupPageComponent } from './backup-page/backup-page.component';

const routes: Routes = [
  { path: 'series', component: SeriesPageComponent },
  { path: 'peliculas', component: PeliculasPageComponent },
  { path: 'mangas', component: MangasPageComponent },
  { path: 'backup', component: BackupPageComponent },
  { path: '', redirectTo: '/series', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}

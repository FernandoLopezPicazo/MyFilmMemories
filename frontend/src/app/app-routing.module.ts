import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SeriesPageComponent } from './series-page/series-page.component';
import { PeliculasPageComponent } from './peliculas-page/peliculas-page.component';
import { MangasPageComponent } from './mangas-page/mangas-page.component';
import { BackupPageComponent } from './backup-page/backup-page.component';
import { LoginComponent } from './login/login.component';
import { AuthGuard } from './auth.guard';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'series', component: SeriesPageComponent, canActivate: [AuthGuard] },
  { path: 'peliculas', component: PeliculasPageComponent, canActivate: [AuthGuard] },
  { path: 'mangas', component: MangasPageComponent, canActivate: [AuthGuard] },
  { path: 'backup', component: BackupPageComponent, canActivate: [AuthGuard] },
  { path: '', redirectTo: '/series', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}

import { Component, OnInit } from '@angular/core';
import { HorarioService, ItemProgramado } from '../horario.service';

const DIAS_ORDEN: ItemProgramado['diaSemana'][] =
  ['LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES', 'SABADO', 'DOMINGO'];

const ETIQUETAS_DIA: Record<string, string> = {
  LUNES: 'Lunes', MARTES: 'Martes', MIERCOLES: 'Miércoles', JUEVES: 'Jueves',
  VIERNES: 'Viernes', SABADO: 'Sábado', DOMINGO: 'Domingo'
};

const ICONO_TIPO: Record<string, string> = {
  SERIE: '📺', PELICULA: '🎬', MANGA: '📖'
};

interface DiaHorario {
  clave: string;
  etiqueta: string;
  esHoy: boolean;
  items: ItemProgramado[];
}

@Component({
  selector: 'app-horario-page',
  templateUrl: './horario-page.component.html',
  styleUrls: ['./horario-page.component.css']
})
export class HorarioPageComponent implements OnInit {

  dias: DiaHorario[] = [];
  cargando = true;
  error = '';

  readonly ICONO_TIPO = ICONO_TIPO;

  constructor(private horarioService: HorarioService) {}

  ngOnInit(): void {
    this.cargarHorario();
  }

  cargarHorario(): void {
    this.cargando = true;
    this.error = '';
    this.horarioService.obtenerHorario().subscribe({
      next: (items) => {
        this.dias = this.construirDias(items);
        this.cargando = false;
      },
      error: () => {
        this.error = 'No se pudo cargar el horario.';
        this.cargando = false;
      }
    });
  }

  private construirDias(items: ItemProgramado[]): DiaHorario[] {
    const hoy = new Date();
    const diaHoyIdx = hoy.getDay(); // 0=domingo..6=sábado
    const diaHoy = DIAS_ORDEN[(diaHoyIdx + 6) % 7]; // reindexar a LUNES..DOMINGO
    const semanaActual = Math.ceil(hoy.getDate() / 7);

    // Los mensuales solo cuentan la semana del mes que les corresponde;
    // los semanales siempre.
    const visibles = items.filter(i =>
      i.frecuencia !== 'MENSUAL' || i.semanaDelMes === semanaActual
    );

    return DIAS_ORDEN.map(dia => ({
      clave: dia,
      etiqueta: ETIQUETAS_DIA[dia],
      esHoy: dia === diaHoy,
      items: visibles.filter(i => i.diaSemana === dia)
    }));
  }
}

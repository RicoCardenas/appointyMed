package com.appointy.catalog.domain.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appointy.catalog.domain.exception.ConflictoException;
import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.exception.ServicioNoDisponibleException;
import com.appointy.catalog.domain.exception.ValidacionException;
import com.appointy.catalog.domain.model.Centro;
import com.appointy.catalog.domain.model.HorarioAtencion;
import com.appointy.catalog.domain.model.ParametroServicio;
import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.domain.model.ResultadoHorario;
import com.appointy.catalog.domain.usecase.HorarioUseCase;
import com.appointy.catalog.domain.gateway.CentroRepositoryGateway;
import com.appointy.catalog.domain.gateway.HorarioRepositoryGateway;
import com.appointy.catalog.domain.gateway.ParametroRepositoryGateway;
import com.appointy.catalog.domain.gateway.ProfesionalRepositoryGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HorariosUseCasesTest {

    @Mock ProfesionalRepositoryGateway profesionales;
    @Mock CentroRepositoryGateway centros;
    @Mock HorarioRepositoryGateway horarios;
    @Mock ParametroRepositoryGateway parametros;
    @Mock PublicadorDeFranjas publicador;

    final Profesional ana = new Profesional("p1", "u1", "Ana", "Perez", "RM-1", true);
    final Centro sede = new Centro("c1", "Sede", "Calle 1", "Cali", null, true);

    private HorarioUseCase definir() {
        return new HorarioUseCase(profesionales, centros, horarios, parametros, publicador, null, null, null, Dobles.TX, Dobles.IDS);
    }

    private void profesionalYCentroValidos() {
        when(profesionales.buscarPorId("p1")).thenReturn(ana);
        when(centros.buscarPorId("c1")).thenReturn(sede);
        when(profesionales.atiendeEnCentro("p1", "c1")).thenReturn(true);
    }

    @Test
    void definirUsaLaDuracionDelParametroSiNoSeIndicaYPideLasFranjas() {
        profesionalYCentroValidos();
        when(parametros.buscarPorClave("duracion_franja_min")).thenReturn(new ParametroServicio("duracion_franja_min", "30", null));
        when(horarios.listarDeProfesional("p1")).thenReturn(new HorarioAtencion[0]);
        when(horarios.guardar(any())).thenAnswer(i -> i.getArgument(0));
        when(publicador.publicar(any())).thenAnswer(i -> new ResultadoHorario(i.getArgument(0), true, 8, 0, "Franjas generadas"));

        ResultadoHorario r = definir().definirHorario(new HorarioUseCase.DefinirHorarioComando("p1", "c1", 1, 480, 600, null));

        assertEquals(30, r.horario().duracionFranjaMin());
        assertTrue(r.horario().activo());
        assertTrue(r.franjasGeneradas());
        verify(publicador).publicar(r.horario());
    }

    @Test
    void definirSiAppointmentFallaElHorarioQuedaGuardado() {
        profesionalYCentroValidos();
        when(horarios.listarDeProfesional("p1")).thenReturn(new HorarioAtencion[0]);
        when(horarios.guardar(any())).thenAnswer(i -> i.getArgument(0));
        when(publicador.publicar(any())).thenAnswer(i -> new ResultadoHorario(i.getArgument(0), false, 0, 0, "se guardo"));

        ResultadoHorario r = definir().definirHorario(new HorarioUseCase.DefinirHorarioComando("p1", "c1", 2, 480, 600, 30));

        assertFalse(r.franjasGeneradas());
        verify(horarios).guardar(r.horario());
    }

    @Test
    void definirRechazaUnCentroDondeElProfesionalNoAtiende() {
        when(profesionales.buscarPorId("p1")).thenReturn(ana);
        when(centros.buscarPorId("c1")).thenReturn(sede);
        when(profesionales.atiendeEnCentro("p1", "c1")).thenReturn(false);

        ValidacionException ex = assertThrows(ValidacionException.class, () -> definir().definirHorario(new HorarioUseCase.DefinirHorarioComando("p1", "c1", 1, 480, 600, 30)));

        assertEquals("PROFESIONAL_NO_ATIENDE_EN_CENTRO", ex.codigo());
        verify(horarios, never()).guardar(any());
    }

    @Test
    void definirRechazaTraslapeYDuracionQueNoDivide() {
        profesionalYCentroValidos();
        when(horarios.listarDeProfesional("p1")).thenReturn(new HorarioAtencion[] {new HorarioAtencion("h0", "p1", "c9", 1, 540, 660, 30, true)});

        assertEquals("HORARIO_TRASLAPADO", assertThrows(ConflictoException.class,
                () -> definir().definirHorario(new HorarioUseCase.DefinirHorarioComando("p1", "c1", 1, 480, 600, 30))).codigo());
        assertEquals("DURACION_NO_DIVIDE_RANGO", assertThrows(ValidacionException.class,
                () -> definir().definirHorario(new HorarioUseCase.DefinirHorarioComando("p1", "c1", 3, 480, 600, 45))).codigo());
        verify(horarios, never()).guardar(any());
    }

    @Test
    void definirRechazaProfesionalInexistenteOInactivoYCentroInactivo() {
        when(profesionales.buscarPorId("nada")).thenReturn(null);
        when(profesionales.buscarPorId("inactivo")).thenReturn(ana.desactivar());
        assertThrows(NoEncontradoException.class, () -> definir().definirHorario(new HorarioUseCase.DefinirHorarioComando("nada", "c1", 1, 480, 600, 30)));
        assertEquals("PROFESIONAL_INACTIVO", assertThrows(ValidacionException.class,
                () -> definir().definirHorario(new HorarioUseCase.DefinirHorarioComando("inactivo", "c1", 1, 480, 600, 30))).codigo());

        when(profesionales.buscarPorId("p1")).thenReturn(ana);
        when(centros.buscarPorId("c1")).thenReturn(sede.desactivar());
        assertEquals("CENTRO_INACTIVO", assertThrows(ValidacionException.class,
                () -> definir().definirHorario(new HorarioUseCase.DefinirHorarioComando("p1", "c1", 1, 480, 600, 30))).codigo());
    }

    @Test
    void modificarNoSeTraslapaConSiMismoYGeneraLasFranjasNuevas() {
        HorarioAtencion actual = new HorarioAtencion("h1", "p1", "c1", 1, 480, 600, 30, true);
        when(horarios.buscarPorId("h1")).thenReturn(actual);
        when(centros.buscarPorId("c1")).thenReturn(sede);
        when(profesionales.atiendeEnCentro("p1", "c1")).thenReturn(true);
        when(horarios.listarDeProfesional("p1")).thenReturn(new HorarioAtencion[] {actual});
        when(horarios.guardar(any())).thenAnswer(i -> i.getArgument(0));
        when(publicador.publicar(any())).thenAnswer(i -> new ResultadoHorario(i.getArgument(0), true, 2, 8, "Franjas generadas"));

        ResultadoHorario r = new HorarioUseCase(profesionales, centros, horarios, parametros, publicador, null, null, null, Dobles.TX, Dobles.IDS)
                .modificarHorario(new HorarioUseCase.ModificarHorarioComando("h1", "c1", 1, 480, 660, 30, true));

        assertEquals(660, r.horario().horaFin());
        assertEquals("p1", r.horario().profesionalId());
    }

    @Test
    void modificarUnHorarioInexistenteFalla() {
        when(horarios.buscarPorId("nada")).thenReturn(null);

        assertThrows(NoEncontradoException.class,
                () -> new HorarioUseCase(profesionales, centros, horarios, parametros, publicador, null, null, null, Dobles.TX, Dobles.IDS)
                        .modificarHorario(new HorarioUseCase.ModificarHorarioComando("nada", "c1", 1, 480, 600, 30, true)));
    }

    @Test
    void reintentarFranjasInformaUn503SiAppointmentSigueCaido() {
        HorarioAtencion h = new HorarioAtencion("h1", "p1", "c1", 1, 480, 600, 30, true);
        when(horarios.buscarPorId("h1")).thenReturn(h);
        when(publicador.publicar(h)).thenReturn(new ResultadoHorario(h, false, 0, 0, "caido"));

        assertThrows(ServicioNoDisponibleException.class, () -> new HorarioUseCase(null, null, horarios, null, publicador, null, null, null, Dobles.TX, Dobles.IDS).generarFranjas("h1"));
    }

    @Test
    void reintentarFranjasDeUnHorarioInactivoEsUnConflicto() {
        when(horarios.buscarPorId("h1")).thenReturn(new HorarioAtencion("h1", "p1", "c1", 1, 480, 600, 30, false));

        assertEquals("HORARIO_INACTIVO", assertThrows(ConflictoException.class, () -> new HorarioUseCase(null, null, horarios, null, publicador, null, null, null, Dobles.TX, Dobles.IDS).generarFranjas("h1")).codigo());
    }
}

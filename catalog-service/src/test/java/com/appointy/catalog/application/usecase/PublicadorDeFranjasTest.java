package com.appointy.catalog.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.appointy.catalog.domain.exception.ServicioNoDisponibleException;
import com.appointy.catalog.domain.model.FranjaSolicitada;
import com.appointy.catalog.domain.model.HorarioAtencion;
import com.appointy.catalog.domain.model.ParametroServicio;
import com.appointy.catalog.domain.model.ResultadoGeneracion;
import com.appointy.catalog.domain.model.ResultadoHorario;
import com.appointy.catalog.domain.port.out.AppointmentClientPort;
import com.appointy.catalog.domain.port.out.ParametroRepositoryPort;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PublicadorDeFranjasTest {

    @Mock ParametroRepositoryPort parametros;
    @Mock AppointmentClientPort appointment;

    /** 2026-06-02T03:00Z es el 2026-06-01 a las 22:00 en Bogota: "hoy" para la clinica es el lunes 1. */
    final Clock reloj = Clock.fixed(Instant.parse("2026-06-02T03:00:00Z"), ZoneId.of("America/Bogota"));

    private HorarioAtencion lunes(boolean activo) {
        return new HorarioAtencion("h1", "p1", "c1", 1, 480, 540, 30, activo);
    }

    @Test
    void generaDesdeElDiaLocalDeLaClinicaYNoDesdeElDiaUtc() {
        when(parametros.buscarPorClave("horizonte_max_dias")).thenReturn(new ParametroServicio("horizonte_max_dias", "7", null));
        when(appointment.generarFranjas(eq("p1"), eq("c1"), any())).thenReturn(new ResultadoGeneracion(4, 0));

        ResultadoHorario r = new PublicadorDeFranjas(parametros, appointment, reloj).publicar(lunes(true));

        ArgumentCaptor<FranjaSolicitada[]> franjas = ArgumentCaptor.forClass(FranjaSolicitada[].class);
        verify(appointment).generarFranjas(eq("p1"), eq("c1"), franjas.capture());
        assertEquals("2026-06-01", franjas.getValue()[0].fecha());
        assertEquals(4, franjas.getValue().length); // lunes 1 y lunes 8, dos franjas cada uno
        assertTrue(r.franjasGeneradas());
        assertEquals(4, r.franjasCreadas());
    }

    @Test
    void siAppointmentFallaElHorarioQuedaGuardadoYSeInforma() {
        when(parametros.buscarPorClave("horizonte_max_dias")).thenReturn(new ParametroServicio("horizonte_max_dias", "7", null));
        when(appointment.generarFranjas(any(), any(), any())).thenThrow(new ServicioNoDisponibleException("APPOINTMENT_NO_DISPONIBLE", "caido"));

        ResultadoHorario r = new PublicadorDeFranjas(parametros, appointment, reloj).publicar(lunes(true));

        assertFalse(r.franjasGeneradas());
        assertTrue(r.mensaje().contains("se guardo"));
    }

    @Test
    void unHorarioInactivoNoGeneraFranjas() {
        ResultadoHorario r = new PublicadorDeFranjas(parametros, appointment, reloj).publicar(lunes(false));

        assertFalse(r.franjasGeneradas());
        verifyNoInteractions(appointment, parametros);
    }
}

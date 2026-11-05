package com.appointy.appointment.domain.rule;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.appointy.appointment.domain.exception.AccesoProhibidoException;
import com.appointy.appointment.domain.exception.ConflictoException;
import com.appointy.appointment.domain.exception.ReglaNegocioException;
import com.appointy.appointment.domain.exception.ValidacionException;
import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.AsociacionProfesional;
import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.EstadoCita;
import com.appointy.appointment.domain.model.EstadoFranja;
import com.appointy.appointment.domain.model.Franja;
import org.junit.jupiter.api.Test;

class ReglasCitaTest {

    static Franja franja(String fecha, int ini, int fin, EstadoFranja estado) {
        return new Franja("f1", "prof", "centro", fecha, ini, fin, estado, 0L);
    }

    static Cita cita(EstadoCita estado) {
        return new Cita("c1", "pac", "prof", "esp", "centro", "f1", estado, 0, 0, 0L);
    }

    @Test
    void franjaOcupadaOBloqueadaEsConflicto() {
        for (EstadoFranja e : new EstadoFranja[] {EstadoFranja.OCUPADA, EstadoFranja.BLOQUEADA}) {
            ConflictoException ex = assertThrows(ConflictoException.class,
                    () -> ReglasCita.exigirFranjaReservable(franja("2030-06-11", 480, 510, e), 10000, 0, 2, "2030-08-01"));
            assertEquals("FRANJA_NO_DISPONIBLE", ex.codigo());
        }
    }

    @Test
    void exigeLaAnticipacionMinimaEnElBorde() {
        Franja f = franja("2030-06-11", 480, 510, EstadoFranja.DISPONIBLE);
        assertDoesNotThrow(() -> ReglasCita.exigirFranjaReservable(f, 120, 0, 2, "2030-08-01"));
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> ReglasCita.exigirFranjaReservable(f, 119, 0, 2, "2030-08-01"));
        assertEquals("ANTICIPACION_INSUFICIENTE", ex.codigo());
    }

    @Test
    void rechazaFranjasMasAllaDelHorizonte() {
        Franja f = franja("2030-08-02", 480, 510, EstadoFranja.DISPONIBLE);
        assertEquals("FUERA_DEL_HORIZONTE", assertThrows(ReglaNegocioException.class,
                () -> ReglasCita.exigirFranjaReservable(f, 1000, 0, 2, "2030-08-01")).codigo());
        assertDoesNotThrow(() -> ReglasCita.exigirFranjaReservable(franja("2030-08-01", 480, 510, EstadoFranja.DISPONIBLE), 1000, 0, 2, "2030-08-01"));
    }

    @Test
    void laFranjaDebeSerDelProfesionalYCentroIndicados() {
        Franja f = franja("2030-06-11", 480, 510, EstadoFranja.DISPONIBLE);
        assertDoesNotThrow(() -> ReglasCita.exigirFranjaDelProfesional(f, "prof", "centro"));
        assertThrows(ReglaNegocioException.class, () -> ReglasCita.exigirFranjaDelProfesional(f, "otro", "centro"));
        assertThrows(ReglaNegocioException.class, () -> ReglasCita.exigirFranjaDelProfesional(f, "prof", "otro"));
    }

    @Test
    void profesionalInactivoONoAsociadoEs422() {
        assertDoesNotThrow(() -> ReglasCita.exigirProfesionalAsociado(new AsociacionProfesional(true, true)));
        assertThrows(ReglaNegocioException.class, () -> ReglasCita.exigirProfesionalAsociado(new AsociacionProfesional(true, false)));
        assertThrows(ReglaNegocioException.class, () -> ReglasCita.exigirProfesionalAsociado(new AsociacionProfesional(false, true)));
    }

    @Test
    void cruceDeFranjasDelPaciente() {
        Franja nueva = franja("2030-06-11", 480, 510, EstadoFranja.DISPONIBLE);
        Franja[] cruzada = {franja("2030-06-11", 495, 525, EstadoFranja.OCUPADA)};
        Franja[] contigua = {franja("2030-06-11", 510, 540, EstadoFranja.OCUPADA)};
        Franja[] otroDia = {franja("2030-06-12", 480, 510, EstadoFranja.OCUPADA)};
        assertEquals("CRUCE_DE_CITAS", assertThrows(ConflictoException.class, () -> ReglasCita.exigirSinCruce(nueva, cruzada)).codigo());
        assertDoesNotThrow(() -> ReglasCita.exigirSinCruce(nueva, contigua));
        assertDoesNotThrow(() -> ReglasCita.exigirSinCruce(nueva, otroDia));
        assertDoesNotThrow(() -> ReglasCita.exigirSinCruce(nueva, new Franja[0]));
    }

    @Test
    void soloUnaCitaConfirmadaSePuedeCambiar() {
        assertDoesNotThrow(() -> ReglasCita.exigirConfirmada(cita(EstadoCita.CONFIRMADA)));
        for (EstadoCita e : new EstadoCita[] {EstadoCita.CANCELADA, EstadoCita.COMPLETADA, EstadoCita.NO_ASISTIDA}) {
            assertEquals("CITA_NO_MODIFICABLE", assertThrows(ConflictoException.class, () -> ReglasCita.exigirConfirmada(cita(e))).codigo());
        }
    }

    @Test
    void propiedadDeLaCita() {
        Cita c = cita(EstadoCita.CONFIRMADA);
        Actor duenio = new Actor("pac", "PACIENTE");
        Actor otro = new Actor("otro", "PACIENTE");
        Actor admin = new Actor("adm", "ADMINISTRATIVO");
        assertDoesNotThrow(() -> ReglasCita.exigirPacienteDuenio(c, duenio));
        assertThrows(AccesoProhibidoException.class, () -> ReglasCita.exigirPacienteDuenio(c, otro));
        assertThrows(AccesoProhibidoException.class, () -> ReglasCita.exigirPacienteDuenio(c, admin));
        assertDoesNotThrow(() -> ReglasCita.exigirDuenioOAdministrativo(c, duenio));
        assertDoesNotThrow(() -> ReglasCita.exigirDuenioOAdministrativo(c, admin));
        assertThrows(AccesoProhibidoException.class, () -> ReglasCita.exigirDuenioOAdministrativo(c, otro));
        assertThrows(AccesoProhibidoException.class, () -> ReglasCita.exigirDuenioOAdministrativo(c, new Actor("pac", "PROFESIONAL")));
    }

    @Test
    void plazoDeCancelacionYDeReprogramacion() {
        assertDoesNotThrow(() -> ReglasCita.exigirDentroDelLimiteDeCancelacion(24 * 60, 0, 24));
        assertEquals("FUERA_DEL_PLAZO_DE_CANCELACION", assertThrows(ReglaNegocioException.class,
                () -> ReglasCita.exigirDentroDelLimiteDeCancelacion(24 * 60 - 1, 0, 24)).codigo());
        assertDoesNotThrow(() -> ReglasCita.exigirAnticipacionParaReprogramar(120, 0, 2));
        assertThrows(ReglaNegocioException.class, () -> ReglasCita.exigirAnticipacionParaReprogramar(119, 0, 2));
    }

    @Test
    void soloSePuedeMarcarCompletadaONoAsistida() {
        assertDoesNotThrow(() -> ReglasCita.exigirEstadoDeAtencion(EstadoCita.COMPLETADA));
        assertDoesNotThrow(() -> ReglasCita.exigirEstadoDeAtencion(EstadoCita.NO_ASISTIDA));
        assertThrows(ValidacionException.class, () -> ReglasCita.exigirEstadoDeAtencion(EstadoCita.CANCELADA));
        assertThrows(ValidacionException.class, () -> ReglasCita.exigirEstadoDeAtencion(EstadoCita.CONFIRMADA));
    }

    @Test
    void elProfesionalDebeSerElDeLaCita() {
        assertDoesNotThrow(() -> ReglasCita.exigirProfesionalDeLaCita(cita(EstadoCita.CONFIRMADA), "prof"));
        assertThrows(AccesoProhibidoException.class, () -> ReglasCita.exigirProfesionalDeLaCita(cita(EstadoCita.CONFIRMADA), "otro"));
    }
}

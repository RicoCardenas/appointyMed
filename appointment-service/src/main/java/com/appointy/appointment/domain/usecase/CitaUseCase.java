package com.appointy.appointment.domain.usecase;

import com.appointy.appointment.domain.gateway.CatalogClientGateway;
import com.appointy.appointment.domain.gateway.CitaRepositoryGateway;
import com.appointy.appointment.domain.gateway.FranjaRepositoryGateway;
import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.AsociacionProfesional;
import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.CitaConFranja;
import com.appointy.appointment.domain.model.EstadoCita;
import com.appointy.appointment.domain.model.Franja;
import com.appointy.appointment.domain.model.ParametrosCita;
import com.appointy.appointment.domain.rule.ReglasCita;

/** reservar cita, reprogramar cita, cancelar cita, marcar cita atendida. */
public class CitaUseCase {

    public record ReservarCitaComando(Actor actor, String especialidadId, String profesionalId, String centroId, String franjaId) {}

    public record ReprogramarCitaComando(Actor actor, String citaId, String nuevaFranjaId) {}

    public record CancelarCitaComando(Actor actor, String citaId, String motivo) {}

    private final CatalogClientGateway catalog;
    private final ReservarCitaTransaccion reservarTransaccion;
    private final ReprogramarCitaTransaccion reprogramarTransaccion;
    private final CancelarCitaTransaccion cancelarTransaccion;
    private final MarcarCitaAtendidaTransaccion marcarTransaccion;
    private final CitaRepositoryGateway citas;
    private final FranjaRepositoryGateway franjas;

    public CitaUseCase(CatalogClientGateway catalog, ReservarCitaTransaccion reservarTransaccion, ReprogramarCitaTransaccion reprogramarTransaccion,
            CancelarCitaTransaccion cancelarTransaccion, MarcarCitaAtendidaTransaccion marcarTransaccion, CitaRepositoryGateway citas, FranjaRepositoryGateway franjas) {
        this.catalog = catalog;
        this.reservarTransaccion = reservarTransaccion;
        this.reprogramarTransaccion = reprogramarTransaccion;
        this.cancelarTransaccion = cancelarTransaccion;
        this.marcarTransaccion = marcarTransaccion;
        this.citas = citas;
        this.franjas = franjas;
    }

    /** CU-18. */
    public CitaConFranja reservarCita(ReservarCitaComando comando) {
        ParametrosCita parametros = catalog.obtenerParametros();
        AsociacionProfesional asociacion = catalog.validarAsociacion(comando.profesionalId(), comando.especialidadId(), comando.centroId());
        ReglasCita.exigirProfesionalAsociado(asociacion);
        return reservarTransaccion.confirmar(comando, parametros);
    }

    /** CU-19. */
    public CitaConFranja reprogramarCita(ReprogramarCitaComando comando) {
        Cita cita = AyudaCitas.buscarCita(citas, comando.citaId());
        ReglasCita.exigirPacienteDuenio(cita, comando.actor());
        ReglasCita.exigirConfirmada(cita);
        Franja nueva = AyudaCitas.buscarFranja(franjas, comando.nuevaFranjaId());
        ParametrosCita parametros = catalog.obtenerParametros();
        AsociacionProfesional asociacion = catalog.validarAsociacion(nueva.profesionalId(), cita.especialidadId(), nueva.centroId());
        ReglasCita.exigirProfesionalAsociado(asociacion);
        return reprogramarTransaccion.reprogramar(comando, cita.franjaId(), parametros);
    }

    /** CU-20. {@code motivo} puede ser null. */
    public CitaConFranja cancelarCita(CancelarCitaComando comando) {
        Cita cita = AyudaCitas.buscarCita(citas, comando.citaId());
        ReglasCita.exigirDuenioOAdministrativo(cita, comando.actor());
        ReglasCita.exigirConfirmada(cita);
        return cancelarTransaccion.cancelar(comando, cita.franjaId(), catalog.obtenerParametros());
    }

    /** CU-23: el profesional de la cita la marca COMPLETADA o NO_ASISTIDA. */
    public CitaConFranja marcarCitaAtendida(Actor actor, String citaId, EstadoCita nuevoEstado) {
        ReglasCita.exigirEstadoDeAtencion(nuevoEstado);
        String profesionalId = AyudaCitas.profesionalDe(catalog, actor);
        Cita cita = AyudaCitas.buscarCita(citas, citaId);
        ReglasCita.exigirProfesionalDeLaCita(cita, profesionalId);
        ReglasCita.exigirConfirmada(cita);
        return marcarTransaccion.marcar(actor, citaId, cita.franjaId(), profesionalId, nuevoEstado);
    }
}

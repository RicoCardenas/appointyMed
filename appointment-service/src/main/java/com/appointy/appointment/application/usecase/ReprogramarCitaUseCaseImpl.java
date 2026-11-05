package com.appointy.appointment.application.usecase;

import com.appointy.appointment.domain.model.AsociacionProfesional;
import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.CitaConFranja;
import com.appointy.appointment.domain.model.Franja;
import com.appointy.appointment.domain.model.ParametrosCita;
import com.appointy.appointment.domain.usecase.ReprogramarCitaUseCase;
import com.appointy.appointment.domain.gateway.CatalogClientGateway;
import com.appointy.appointment.domain.gateway.CitaRepositoryGateway;
import com.appointy.appointment.domain.gateway.FranjaRepositoryGateway;
import com.appointy.appointment.domain.rule.ReglasCita;

/** CU-19. Valida primero la franja nueva con las reglas de CU-18; la transaccion solo corre si todo lo anterior paso. */
public class ReprogramarCitaUseCaseImpl implements ReprogramarCitaUseCase {

    private final CitaRepositoryGateway citas;
    private final FranjaRepositoryGateway franjas;
    private final CatalogClientGateway catalog;
    private final ReprogramarCitaTransaccion transaccion;

    public ReprogramarCitaUseCaseImpl(CitaRepositoryGateway citas, FranjaRepositoryGateway franjas, CatalogClientGateway catalog,
            ReprogramarCitaTransaccion transaccion) {
        this.citas = citas;
        this.franjas = franjas;
        this.catalog = catalog;
        this.transaccion = transaccion;
    }

    @Override
    public CitaConFranja ejecutar(Comando comando) {
        Cita cita = AyudaCitas.buscarCita(citas, comando.citaId());
        ReglasCita.exigirPacienteDuenio(cita, comando.actor());
        ReglasCita.exigirConfirmada(cita);
        Franja nueva = AyudaCitas.buscarFranja(franjas, comando.nuevaFranjaId());
        ParametrosCita parametros = catalog.obtenerParametros();
        AsociacionProfesional asociacion = catalog.validarAsociacion(nueva.profesionalId(), cita.especialidadId(), nueva.centroId());
        ReglasCita.exigirProfesionalAsociado(asociacion);
        return transaccion.reprogramar(comando, cita.franjaId(), parametros);
    }
}

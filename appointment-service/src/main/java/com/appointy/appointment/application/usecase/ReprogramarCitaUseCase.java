package com.appointy.appointment.application.usecase;

import com.appointy.appointment.domain.model.AsociacionProfesional;
import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.CitaConFranja;
import com.appointy.appointment.domain.model.Franja;
import com.appointy.appointment.domain.model.ParametrosCita;
import com.appointy.appointment.domain.port.in.ReprogramarCitaPort;
import com.appointy.appointment.domain.port.out.CatalogClientPort;
import com.appointy.appointment.domain.port.out.CitaRepositoryPort;
import com.appointy.appointment.domain.port.out.FranjaRepositoryPort;
import com.appointy.appointment.domain.rule.ReglasCita;

/** CU-19. Valida primero la franja nueva con las reglas de CU-18; la transaccion solo corre si todo lo anterior paso. */
public class ReprogramarCitaUseCase implements ReprogramarCitaPort {

    private final CitaRepositoryPort citas;
    private final FranjaRepositoryPort franjas;
    private final CatalogClientPort catalog;
    private final ReprogramarCitaTransaccion transaccion;

    public ReprogramarCitaUseCase(CitaRepositoryPort citas, FranjaRepositoryPort franjas, CatalogClientPort catalog,
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

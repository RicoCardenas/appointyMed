package com.appointy.appointment.application.usecase;

import com.appointy.appointment.domain.model.AsociacionProfesional;
import com.appointy.appointment.domain.model.CitaConFranja;
import com.appointy.appointment.domain.model.ParametrosCita;
import com.appointy.appointment.domain.usecase.ReservarCitaUseCase;
import com.appointy.appointment.domain.gateway.CatalogClientGateway;
import com.appointy.appointment.domain.rule.ReglasCita;

/**
 * CU-18, pasos 3 a 5a. Consulta a catalog antes de abrir la transaccion para no retener el bloqueo de la franja durante
 * una llamada de red (decision 5); si catalog no responde, 503 y no se guarda nada. La franja se reverifica bajo bloqueo.
 */
public class ReservarCitaUseCaseImpl implements ReservarCitaUseCase {

    private final CatalogClientGateway catalog;
    private final ReservarCitaTransaccion transaccion;

    public ReservarCitaUseCaseImpl(CatalogClientGateway catalog, ReservarCitaTransaccion transaccion) {
        this.catalog = catalog;
        this.transaccion = transaccion;
    }

    @Override
    public CitaConFranja ejecutar(Comando comando) {
        ParametrosCita parametros = catalog.obtenerParametros();
        AsociacionProfesional asociacion = catalog.validarAsociacion(comando.profesionalId(), comando.especialidadId(), comando.centroId());
        ReglasCita.exigirProfesionalAsociado(asociacion);
        return transaccion.confirmar(comando, parametros);
    }
}

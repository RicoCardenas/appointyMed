package com.appointy.appointment.application.usecase;

import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.CitaConFranja;
import com.appointy.appointment.domain.usecase.CancelarCitaUseCase;
import com.appointy.appointment.domain.gateway.CatalogClientGateway;
import com.appointy.appointment.domain.gateway.CitaRepositoryGateway;
import com.appointy.appointment.domain.rule.ReglasCita;

/** CU-20. La verificacion de dueno y estado va antes de consultar a catalog; el limite de cancelacion se aplica bajo bloqueo. */
public class CancelarCitaUseCaseImpl implements CancelarCitaUseCase {

    private final CitaRepositoryGateway citas;
    private final CatalogClientGateway catalog;
    private final CancelarCitaTransaccion transaccion;

    public CancelarCitaUseCaseImpl(CitaRepositoryGateway citas, CatalogClientGateway catalog, CancelarCitaTransaccion transaccion) {
        this.citas = citas;
        this.catalog = catalog;
        this.transaccion = transaccion;
    }

    @Override
    public CitaConFranja ejecutar(Comando comando) {
        Cita cita = AyudaCitas.buscarCita(citas, comando.citaId());
        ReglasCita.exigirDuenioOAdministrativo(cita, comando.actor());
        ReglasCita.exigirConfirmada(cita);
        return transaccion.cancelar(comando, cita.franjaId(), catalog.obtenerParametros());
    }
}

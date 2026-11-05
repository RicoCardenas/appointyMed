package com.appointy.appointment.application.usecase;

import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.CitaConFranja;
import com.appointy.appointment.domain.port.in.CancelarCitaPort;
import com.appointy.appointment.domain.port.out.CatalogClientPort;
import com.appointy.appointment.domain.port.out.CitaRepositoryPort;
import com.appointy.appointment.domain.rule.ReglasCita;

/** CU-20. La verificacion de dueno y estado va antes de consultar a catalog; el limite de cancelacion se aplica bajo bloqueo. */
public class CancelarCitaUseCase implements CancelarCitaPort {

    private final CitaRepositoryPort citas;
    private final CatalogClientPort catalog;
    private final CancelarCitaTransaccion transaccion;

    public CancelarCitaUseCase(CitaRepositoryPort citas, CatalogClientPort catalog, CancelarCitaTransaccion transaccion) {
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

package com.appointy.clinicalhistory.application.usecase;

import com.appointy.clinicalhistory.domain.gateway.EntradaClinicaRepositoryGateway;
import com.appointy.clinicalhistory.domain.gateway.LogAccesoRepositoryGateway;
import com.appointy.clinicalhistory.domain.model.AccionAcceso;
import com.appointy.clinicalhistory.domain.model.Actor;
import com.appointy.clinicalhistory.domain.model.EntradaClinica;
import com.appointy.clinicalhistory.domain.model.ResultadoAcceso;
import org.springframework.transaction.annotation.Transactional;

/** La entrada y su log de acceso se guardan juntos o no se guarda ninguno: no hay escritura sin rastro. */
public class GuardarEntradaTransaccion {

    private final EntradaClinicaRepositoryGateway entradas;
    private final LogAccesoRepositoryGateway logs;
    private final Auditoria auditoria;

    public GuardarEntradaTransaccion(EntradaClinicaRepositoryGateway entradas, LogAccesoRepositoryGateway logs, Auditoria auditoria) {
        this.entradas = entradas;
        this.logs = logs;
        this.auditoria = auditoria;
    }

    @Transactional
    public EntradaClinica guardar(Actor actor, EntradaClinica entrada) {
        entradas.registrar(entrada);
        logs.registrar(auditoria.nuevo(actor, entrada.pacienteId(), AccionAcceso.ESCRITURA, entrada.id(), ResultadoAcceso.PERMITIDO));
        return entrada;
    }
}

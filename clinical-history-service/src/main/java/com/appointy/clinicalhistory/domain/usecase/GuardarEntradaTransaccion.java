package com.appointy.clinicalhistory.domain.usecase;

import com.appointy.clinicalhistory.domain.gateway.TransactionGateway;
import com.appointy.clinicalhistory.domain.gateway.EntradaClinicaRepositoryGateway;
import com.appointy.clinicalhistory.domain.gateway.LogAccesoRepositoryGateway;
import com.appointy.clinicalhistory.domain.model.AccionAcceso;
import com.appointy.clinicalhistory.domain.model.Actor;
import com.appointy.clinicalhistory.domain.model.EntradaClinica;
import com.appointy.clinicalhistory.domain.model.ResultadoAcceso;

/** La entrada y su log de acceso se guardan juntos o no se guarda ninguno: no hay escritura sin rastro. */
public class GuardarEntradaTransaccion {

    private final EntradaClinicaRepositoryGateway entradas;
    private final LogAccesoRepositoryGateway logs;
    private final Auditoria auditoria;
    private final TransactionGateway transacciones;

    public GuardarEntradaTransaccion(EntradaClinicaRepositoryGateway entradas, LogAccesoRepositoryGateway logs, Auditoria auditoria, TransactionGateway transacciones) {
        this.transacciones = transacciones;
        this.entradas = entradas;
        this.logs = logs;
        this.auditoria = auditoria;
    }

    public EntradaClinica guardar(Actor actor, EntradaClinica entrada) {
        return transacciones.ejecutar(() -> {
            entradas.registrar(entrada);
            logs.registrar(auditoria.nuevo(actor, entrada.pacienteId(), AccionAcceso.ESCRITURA, entrada.id(), ResultadoAcceso.PERMITIDO));
            return entrada;
    
        });
    }
}

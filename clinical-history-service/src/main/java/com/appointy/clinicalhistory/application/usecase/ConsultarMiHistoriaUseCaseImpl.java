package com.appointy.clinicalhistory.application.usecase;

import com.appointy.clinicalhistory.domain.exception.AccesoProhibidoException;
import com.appointy.clinicalhistory.domain.gateway.EntradaClinicaRepositoryGateway;
import com.appointy.clinicalhistory.domain.model.AccionAcceso;
import com.appointy.clinicalhistory.domain.model.Actor;
import com.appointy.clinicalhistory.domain.model.EntradaClinica;
import com.appointy.clinicalhistory.domain.usecase.ConsultarMiHistoriaUseCase;

/** CU-30: el paciente es su propia cuenta; no hay parametro de paciente que manipular. */
public class ConsultarMiHistoriaUseCaseImpl implements ConsultarMiHistoriaUseCase {

    private final EntradaClinicaRepositoryGateway entradas;
    private final Auditoria auditoria;

    public ConsultarMiHistoriaUseCaseImpl(EntradaClinicaRepositoryGateway entradas, Auditoria auditoria) {
        this.entradas = entradas;
        this.auditoria = auditoria;
    }

    @Override
    public EntradaClinica[] ejecutar(Actor actor) {
        String pacienteId = actor.esPaciente() ? actor.usuarioId() : null;
        try {
            if (!actor.esPaciente()) {
                throw new AccesoProhibidoException("SOLO_PACIENTE", "Esta operacion es solo para pacientes");
            }
            EntradaClinica[] historia = entradas.listarDePaciente(pacienteId);
            auditoria.permitido(actor, pacienteId, AccionAcceso.LECTURA, null);
            return historia;
        } catch (RuntimeException e) {
            auditoria.denegado(actor, pacienteId, AccionAcceso.LECTURA, null);
            throw e;
        }
    }
}

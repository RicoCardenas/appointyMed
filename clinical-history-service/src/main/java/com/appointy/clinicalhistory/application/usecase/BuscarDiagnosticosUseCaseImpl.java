package com.appointy.clinicalhistory.application.usecase;

import com.appointy.clinicalhistory.domain.gateway.Cie10ClientGateway;
import com.appointy.clinicalhistory.domain.model.Actor;
import com.appointy.clinicalhistory.domain.model.Diagnostico;
import com.appointy.clinicalhistory.domain.rule.ReglasHistoria;
import com.appointy.clinicalhistory.domain.usecase.BuscarDiagnosticosUseCase;

public class BuscarDiagnosticosUseCaseImpl implements BuscarDiagnosticosUseCase {

    private final Cie10ClientGateway cie10;

    public BuscarDiagnosticosUseCaseImpl(Cie10ClientGateway cie10) {
        this.cie10 = cie10;
    }

    @Override
    public Diagnostico[] ejecutar(Actor actor, String termino) {
        AyudaHistoria.exigirProfesional(actor);
        return cie10.buscar(ReglasHistoria.validarTermino(termino), ReglasHistoria.MAXIMO_DIAGNOSTICOS);
    }
}

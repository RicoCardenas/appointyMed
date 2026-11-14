package com.appointy.clinicalhistory.application.usecase;

import com.appointy.clinicalhistory.domain.model.Actor;
import com.appointy.clinicalhistory.domain.model.ResumenHistoria;
import com.appointy.clinicalhistory.domain.rule.ReglasHistoria;
import com.appointy.clinicalhistory.domain.usecase.ConsultarMiHistoriaUseCase;
import com.appointy.clinicalhistory.domain.usecase.ConsultarMiResumenUseCase;

/** Reutiliza CU-30: solo el paciente y siempre su propia historia. */
public class ConsultarMiResumenUseCaseImpl implements ConsultarMiResumenUseCase {

    private final ConsultarMiHistoriaUseCase historia;

    public ConsultarMiResumenUseCaseImpl(ConsultarMiHistoriaUseCase historia) {
        this.historia = historia;
    }

    @Override
    public ResumenHistoria ejecutar(Actor actor) {
        return ReglasHistoria.resumir(historia.ejecutar(actor));
    }
}

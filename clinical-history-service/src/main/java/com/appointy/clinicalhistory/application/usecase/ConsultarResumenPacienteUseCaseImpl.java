package com.appointy.clinicalhistory.application.usecase;

import com.appointy.clinicalhistory.domain.model.Actor;
import com.appointy.clinicalhistory.domain.model.ResumenHistoria;
import com.appointy.clinicalhistory.domain.rule.ReglasHistoria;
import com.appointy.clinicalhistory.domain.usecase.ConsultarHistoriaPacienteUseCase;
import com.appointy.clinicalhistory.domain.usecase.ConsultarResumenPacienteUseCase;

/** Reutiliza CU-29: mismas reglas de acceso y el mismo registro en el log de acceso. */
public class ConsultarResumenPacienteUseCaseImpl implements ConsultarResumenPacienteUseCase {

    private final ConsultarHistoriaPacienteUseCase historia;

    public ConsultarResumenPacienteUseCaseImpl(ConsultarHistoriaPacienteUseCase historia) {
        this.historia = historia;
    }

    @Override
    public ResumenHistoria ejecutar(Actor actor, String pacienteId) {
        return ReglasHistoria.resumir(historia.ejecutar(actor, pacienteId));
    }
}

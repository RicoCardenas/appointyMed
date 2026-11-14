package com.appointy.clinicalhistory.application.usecase;

import com.appointy.clinicalhistory.domain.exception.NoEncontradoException;
import com.appointy.clinicalhistory.domain.gateway.EntradaClinicaRepositoryGateway;
import com.appointy.clinicalhistory.domain.model.AccionAcceso;
import com.appointy.clinicalhistory.domain.model.Actor;
import com.appointy.clinicalhistory.domain.model.EntradaClinica;
import com.appointy.clinicalhistory.domain.usecase.ConsultarHistoriaPacienteUseCase;
import com.appointy.clinicalhistory.domain.usecase.ListarVersionesEntradaUseCase;
import java.util.ArrayList;
import java.util.List;

public class ListarVersionesEntradaUseCaseImpl implements ListarVersionesEntradaUseCase {

    private final EntradaClinicaRepositoryGateway entradas;
    private final ConsultarHistoriaPacienteUseCase historia;
    private final Auditoria auditoria;

    public ListarVersionesEntradaUseCaseImpl(EntradaClinicaRepositoryGateway entradas, ConsultarHistoriaPacienteUseCase historia, Auditoria auditoria) {
        this.entradas = entradas;
        this.historia = historia;
        this.auditoria = auditoria;
    }

    @Override
    public EntradaClinica[] ejecutar(Actor actor, String entradaId) {
        EntradaClinica entrada = entradas.buscarPorId(entradaId);
        if (entrada == null) {
            auditoria.denegado(actor, null, AccionAcceso.LECTURA, entradaId);
            throw new NoEncontradoException("ENTRADA_NO_ENCONTRADA", "La entrada no existe");
        }
        // El acceso (rol, relacion con el paciente y log) lo decide CU-29 sobre el paciente de la entrada.
        List<EntradaClinica> versiones = new ArrayList<>();
        for (EntradaClinica e : historia.ejecutar(actor, entrada.pacienteId())) {
            if (e.citaId().equals(entrada.citaId())) {
                versiones.add(e);
            }
        }
        return versiones.toArray(new EntradaClinica[0]);
    }
}

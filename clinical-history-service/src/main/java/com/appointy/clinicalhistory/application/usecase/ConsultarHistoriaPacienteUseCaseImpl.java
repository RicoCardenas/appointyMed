package com.appointy.clinicalhistory.application.usecase;

import com.appointy.clinicalhistory.domain.exception.AccesoProhibidoException;
import com.appointy.clinicalhistory.domain.gateway.AppointmentClientGateway;
import com.appointy.clinicalhistory.domain.gateway.CatalogClientGateway;
import com.appointy.clinicalhistory.domain.gateway.EntradaClinicaRepositoryGateway;
import com.appointy.clinicalhistory.domain.model.AccionAcceso;
import com.appointy.clinicalhistory.domain.model.Actor;
import com.appointy.clinicalhistory.domain.model.EntradaClinica;
import com.appointy.clinicalhistory.domain.usecase.ConsultarHistoriaPacienteUseCase;

/** CU-29. La relacion profesional-paciente (CU-27) se valida en appointment, con cache de las respuestas afirmativas. */
public class ConsultarHistoriaPacienteUseCaseImpl implements ConsultarHistoriaPacienteUseCase {

    private final CatalogClientGateway catalog;
    private final AppointmentClientGateway appointment;
    private final EntradaClinicaRepositoryGateway entradas;
    private final Auditoria auditoria;

    public ConsultarHistoriaPacienteUseCaseImpl(CatalogClientGateway catalog, AppointmentClientGateway appointment,
            EntradaClinicaRepositoryGateway entradas, Auditoria auditoria) {
        this.catalog = catalog;
        this.appointment = appointment;
        this.entradas = entradas;
        this.auditoria = auditoria;
    }

    @Override
    public EntradaClinica[] ejecutar(Actor actor, String pacienteId) {
        try {
            AyudaHistoria.exigirProfesional(actor);
            String profesionalId = AyudaHistoria.profesionalDe(catalog, actor);
            if (!appointment.existeRelacion(profesionalId, pacienteId)) {
                throw new AccesoProhibidoException("SIN_RELACION_CON_PACIENTE", "No tienes ni tuviste citas con este paciente");
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

package com.appointy.clinicalhistory.application.usecase;

import com.appointy.clinicalhistory.domain.exception.AccesoProhibidoException;
import com.appointy.clinicalhistory.domain.exception.ConflictoException;
import com.appointy.clinicalhistory.domain.exception.NoEncontradoException;
import com.appointy.clinicalhistory.domain.exception.ReglaNegocioException;
import com.appointy.clinicalhistory.domain.gateway.AppointmentClientGateway;
import com.appointy.clinicalhistory.domain.gateway.CatalogClientGateway;
import com.appointy.clinicalhistory.domain.gateway.EntradaClinicaRepositoryGateway;
import com.appointy.clinicalhistory.domain.model.AccionAcceso;
import com.appointy.clinicalhistory.domain.model.Actor;
import com.appointy.clinicalhistory.domain.model.CitaClinica;
import com.appointy.clinicalhistory.domain.model.ContenidoClinico;
import com.appointy.clinicalhistory.domain.model.EntradaClinica;
import com.appointy.clinicalhistory.domain.rule.ReglasHistoria;
import com.appointy.clinicalhistory.domain.usecase.RegistrarAtencionUseCase;
import java.time.Clock;
import java.util.UUID;

/** CU-28. Las consultas a otros servicios van antes de la transaccion, que solo escribe la entrada y su log. */
public class RegistrarAtencionUseCaseImpl implements RegistrarAtencionUseCase {

    private final CatalogClientGateway catalog;
    private final AppointmentClientGateway appointment;
    private final EntradaClinicaRepositoryGateway entradas;
    private final GuardarEntradaTransaccion transaccion;
    private final Auditoria auditoria;
    private final Clock reloj;

    public RegistrarAtencionUseCaseImpl(CatalogClientGateway catalog, AppointmentClientGateway appointment, EntradaClinicaRepositoryGateway entradas,
            GuardarEntradaTransaccion transaccion, Auditoria auditoria, Clock reloj) {
        this.catalog = catalog;
        this.appointment = appointment;
        this.entradas = entradas;
        this.transaccion = transaccion;
        this.auditoria = auditoria;
        this.reloj = reloj;
    }

    @Override
    public EntradaClinica ejecutar(Comando comando) {
        Actor actor = comando.actor();
        String pacienteId = null;
        try {
            AyudaHistoria.exigirProfesional(actor);
            ContenidoClinico c = comando.contenido();
            ReglasHistoria.validarContenido(c);
            String profesionalId = AyudaHistoria.profesionalDe(catalog, actor);
            CitaClinica cita = appointment.buscarCita(comando.citaId());
            if (cita == null) {
                throw new NoEncontradoException("CITA_NO_ENCONTRADA", "La cita no existe");
            }
            pacienteId = cita.pacienteId();
            if (!cita.profesionalId().equals(profesionalId)) {
                throw new AccesoProhibidoException("NO_ES_TU_CITA", "Solo el profesional asignado a la cita puede registrar la atencion");
            }
            if (!ReglasHistoria.citaAtendible(cita.estado())) {
                throw new ReglaNegocioException("CITA_NO_ATENDIBLE", "Solo se registra la atencion de una cita confirmada o completada");
            }
            if (entradas.existeOriginalDeCita(cita.id())) {
                throw new ConflictoException("ATENCION_YA_REGISTRADA", "La cita ya tiene una atencion registrada; para cambiarla crea una correccion");
            }
            return transaccion.guardar(actor, new EntradaClinica(UUID.randomUUID().toString(), pacienteId, profesionalId, cita.id(),
                    c.motivo().strip(), c.diagnostico().strip(), c.notas(), c.indicaciones(), null, reloj.millis()));
        } catch (RuntimeException e) {
            auditoria.denegado(actor, pacienteId, AccionAcceso.ESCRITURA, null);
            throw e;
        }
    }
}

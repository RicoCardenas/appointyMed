package com.appointy.clinicalhistory.domain.usecase;

import com.appointy.clinicalhistory.domain.gateway.RelojGateway;
import com.appointy.clinicalhistory.domain.gateway.GeneradorIdGateway;
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

/** registrar atencion, corregir entrada, listar versiones entrada. */
public class EntradaClinicaUseCase {

    public record RegistrarAtencionComando(Actor actor, String citaId, ContenidoClinico contenido) {}

    public record CorregirEntradaComando(Actor actor, String entradaId, ContenidoClinico contenido) {}

    private final CatalogClientGateway catalog;
    private final AppointmentClientGateway appointment;
    private final EntradaClinicaRepositoryGateway entradas;
    private final GuardarEntradaTransaccion transaccion;
    private final Auditoria auditoria;
    private final RelojGateway reloj;
    private final HistoriaUseCase historia;
    private final GeneradorIdGateway ids;

    public EntradaClinicaUseCase(CatalogClientGateway catalog, AppointmentClientGateway appointment, EntradaClinicaRepositoryGateway entradas, GuardarEntradaTransaccion transaccion, Auditoria auditoria, RelojGateway reloj, HistoriaUseCase historia, GeneradorIdGateway ids) {
        this.ids = ids;
        this.catalog = catalog;
        this.appointment = appointment;
        this.entradas = entradas;
        this.transaccion = transaccion;
        this.auditoria = auditoria;
        this.reloj = reloj;
        this.historia = historia;
    }

    /** CU-28: el profesional de la cita registra la atencion. Cita CONFIRMADA o COMPLETADA; una sola entrada original por cita. */
    public EntradaClinica registrarAtencion(RegistrarAtencionComando comando) {
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
            return transaccion.guardar(actor, new EntradaClinica(ids.nuevoId(), pacienteId, profesionalId, cita.id(),
                    c.motivo().strip(), c.diagnostico().strip(), c.notas(), c.indicaciones(), null, reloj.ahora()));
        } catch (RuntimeException e) {
            auditoria.denegado(actor, pacienteId, AccionAcceso.ESCRITURA, null);
            throw e;
        }
    }

    /** CU-31: solo el autor corrige; la correccion es una entrada nueva que referencia la anterior (RF-29). */
    public EntradaClinica corregirEntrada(CorregirEntradaComando comando) {
        Actor actor = comando.actor();
        String pacienteId = null;
        try {
            AyudaHistoria.exigirProfesional(actor);
            ContenidoClinico c = comando.contenido();
            ReglasHistoria.validarContenido(c);
            String profesionalId = AyudaHistoria.profesionalDe(catalog, actor);
            EntradaClinica original = entradas.buscarPorId(comando.entradaId());
            if (original == null) {
                throw new NoEncontradoException("ENTRADA_NO_ENCONTRADA", "La entrada no existe");
            }
            pacienteId = original.pacienteId();
            if (!original.profesionalId().equals(profesionalId)) {
                throw new AccesoProhibidoException("NO_ES_TU_ENTRADA", "Solo el profesional que la escribio puede corregir una entrada");
            }
            return transaccion.guardar(actor, new EntradaClinica(ids.nuevoId(), pacienteId, profesionalId, original.citaId(),
                    c.motivo().strip(), c.diagnostico().strip(), c.notas(), c.indicaciones(), original.id(), reloj.ahora()));
        } catch (RuntimeException e) {
            auditoria.denegado(actor, pacienteId, AccionAcceso.ESCRITURA, comando.entradaId());
            throw e;
        }
    }

    /** La entrada original de una atencion y todas sus correcciones, por fecha ascendente. Mismas reglas y auditoria que CU-29. */
    public EntradaClinica[] listarVersionesEntrada(Actor actor, String entradaId) {
        EntradaClinica entrada = entradas.buscarPorId(entradaId);
        if (entrada == null) {
            auditoria.denegado(actor, null, AccionAcceso.LECTURA, entradaId);
            throw new NoEncontradoException("ENTRADA_NO_ENCONTRADA", "La entrada no existe");
        }
        // El acceso (rol, relacion con el paciente y log) lo decide CU-29 sobre el paciente de la entrada.
        EntradaClinica[] todas = historia.consultarHistoriaPaciente(actor, entrada.pacienteId());
        EntradaClinica[] versiones = new EntradaClinica[todas.length];
        int n = 0;
        for (EntradaClinica e : todas) {
            if (e.citaId().equals(entrada.citaId())) {
                versiones[n++] = e;
            }
        }
        EntradaClinica[] resultado = new EntradaClinica[n];
        System.arraycopy(versiones, 0, resultado, 0, n);
        return resultado;
    }
}

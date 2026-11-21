package com.appointy.clinicalhistory.domain.usecase;

import com.appointy.clinicalhistory.domain.exception.AccesoProhibidoException;
import com.appointy.clinicalhistory.domain.gateway.AppointmentClientGateway;
import com.appointy.clinicalhistory.domain.gateway.CatalogClientGateway;
import com.appointy.clinicalhistory.domain.gateway.Cie10ClientGateway;
import com.appointy.clinicalhistory.domain.gateway.EntradaClinicaRepositoryGateway;
import com.appointy.clinicalhistory.domain.model.AccionAcceso;
import com.appointy.clinicalhistory.domain.model.Actor;
import com.appointy.clinicalhistory.domain.model.Diagnostico;
import com.appointy.clinicalhistory.domain.model.EntradaClinica;
import com.appointy.clinicalhistory.domain.model.PaginaEntradas;
import com.appointy.clinicalhistory.domain.model.ResumenHistoria;
import com.appointy.clinicalhistory.domain.rule.Arreglos;
import com.appointy.clinicalhistory.domain.rule.ReglasHistoria;

/** consultar historia paciente, consultar mi historia, listar atenciones, consultar mi resumen, consultar resumen paciente, buscar diagnosticos. */
public class HistoriaUseCase {

    private final CatalogClientGateway catalog;
    private final AppointmentClientGateway appointment;
    private final EntradaClinicaRepositoryGateway entradas;
    private final Auditoria auditoria;
    private final Cie10ClientGateway cie10;

    public HistoriaUseCase(CatalogClientGateway catalog, AppointmentClientGateway appointment, EntradaClinicaRepositoryGateway entradas, Auditoria auditoria, Cie10ClientGateway cie10) {
        this.catalog = catalog;
        this.appointment = appointment;
        this.entradas = entradas;
        this.auditoria = auditoria;
        this.cie10 = cie10;
    }

    /** CU-29: un profesional con relacion vigente o pasada con el paciente (CU-27) lee su historia completa. */
    public EntradaClinica[] consultarHistoriaPaciente(Actor actor, String pacienteId) {
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

    /** CU-30: el paciente lee unicamente su propia historia. */
    public EntradaClinica[] consultarMiHistoria(Actor actor) {
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

    /** Listado paginado (RA-04) de las entradas que escribio el profesional autenticado. Cada paciente leido queda en el log de acceso. */
    public PaginaEntradas listarAtenciones(Actor actor, int pagina, int tamano) {
        try {
            AyudaHistoria.exigirProfesional(actor);
            ReglasHistoria.validarPaginacion(pagina, tamano);
            PaginaEntradas resultado = entradas.listarDeProfesional(AyudaHistoria.profesionalDe(catalog, actor), pagina, tamano);
            // RF-30: cada paciente cuya historia aparece en la pagina queda registrado.
            String[] todos = new String[resultado.contenido().length];
            for (int i = 0; i < todos.length; i++) {
                todos[i] = resultado.contenido()[i].pacienteId();
            }
            String[] pacientes = Arreglos.sinDuplicados(todos);
            for (String pacienteId : pacientes) {
                auditoria.permitido(actor, pacienteId, AccionAcceso.LECTURA, null);
            }
            return resultado;
        } catch (RuntimeException e) {
            auditoria.denegado(actor, null, AccionAcceso.LECTURA, null);
            throw e;
        }
    }

    /** Resumen de la propia historia del paciente (mismas reglas y auditoria que CU-30). */
    public ResumenHistoria consultarMiResumen(Actor actor) {
        return ReglasHistoria.resumir(consultarMiHistoria(actor));
    }

    /** Resumen de la historia de un paciente para un profesional con relacion (mismas reglas y auditoria que CU-29). */
    public ResumenHistoria consultarResumenPaciente(Actor actor, String pacienteId) {
        return ReglasHistoria.resumir(consultarHistoriaPaciente(actor, pacienteId));
    }

    /** Busca en el catalogo CIE-10 (API externa). Solo PROFESIONAL; no se registra el termino. */
    public Diagnostico[] buscarDiagnosticos(Actor actor, String termino) {
        AyudaHistoria.exigirProfesional(actor);
        return cie10.buscar(ReglasHistoria.validarTermino(termino), ReglasHistoria.MAXIMO_DIAGNOSTICOS);
    }
}

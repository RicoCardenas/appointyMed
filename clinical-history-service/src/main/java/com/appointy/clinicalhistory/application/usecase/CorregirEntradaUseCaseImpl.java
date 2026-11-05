package com.appointy.clinicalhistory.application.usecase;

import com.appointy.clinicalhistory.domain.exception.AccesoProhibidoException;
import com.appointy.clinicalhistory.domain.exception.NoEncontradoException;
import com.appointy.clinicalhistory.domain.gateway.CatalogClientGateway;
import com.appointy.clinicalhistory.domain.gateway.EntradaClinicaRepositoryGateway;
import com.appointy.clinicalhistory.domain.model.AccionAcceso;
import com.appointy.clinicalhistory.domain.model.Actor;
import com.appointy.clinicalhistory.domain.model.ContenidoClinico;
import com.appointy.clinicalhistory.domain.model.EntradaClinica;
import com.appointy.clinicalhistory.domain.rule.ReglasHistoria;
import com.appointy.clinicalhistory.domain.usecase.CorregirEntradaUseCase;
import java.time.Clock;
import java.util.UUID;

/** CU-31: la entrada original no se toca; se crea otra con el mismo paciente, cita y autor que la referencia (RF-29). */
public class CorregirEntradaUseCaseImpl implements CorregirEntradaUseCase {

    private final CatalogClientGateway catalog;
    private final EntradaClinicaRepositoryGateway entradas;
    private final GuardarEntradaTransaccion transaccion;
    private final Auditoria auditoria;
    private final Clock reloj;

    public CorregirEntradaUseCaseImpl(CatalogClientGateway catalog, EntradaClinicaRepositoryGateway entradas, GuardarEntradaTransaccion transaccion,
            Auditoria auditoria, Clock reloj) {
        this.catalog = catalog;
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
            EntradaClinica original = entradas.buscarPorId(comando.entradaId());
            if (original == null) {
                throw new NoEncontradoException("ENTRADA_NO_ENCONTRADA", "La entrada no existe");
            }
            pacienteId = original.pacienteId();
            if (!original.profesionalId().equals(profesionalId)) {
                throw new AccesoProhibidoException("NO_ES_TU_ENTRADA", "Solo el profesional que la escribio puede corregir una entrada");
            }
            return transaccion.guardar(actor, new EntradaClinica(UUID.randomUUID().toString(), pacienteId, profesionalId, original.citaId(),
                    c.motivo().strip(), c.diagnostico().strip(), c.notas(), c.indicaciones(), original.id(), reloj.millis()));
        } catch (RuntimeException e) {
            auditoria.denegado(actor, pacienteId, AccionAcceso.ESCRITURA, comando.entradaId());
            throw e;
        }
    }
}

package com.appointy.clinicalhistory.application.usecase;

import com.appointy.clinicalhistory.domain.gateway.CatalogClientGateway;
import com.appointy.clinicalhistory.domain.gateway.EntradaClinicaRepositoryGateway;
import com.appointy.clinicalhistory.domain.model.AccionAcceso;
import com.appointy.clinicalhistory.domain.model.Actor;
import com.appointy.clinicalhistory.domain.model.EntradaClinica;
import com.appointy.clinicalhistory.domain.model.PaginaEntradas;
import com.appointy.clinicalhistory.domain.rule.ReglasHistoria;
import com.appointy.clinicalhistory.domain.usecase.ListarAtencionesUseCase;
import java.util.LinkedHashSet;
import java.util.Set;

public class ListarAtencionesUseCaseImpl implements ListarAtencionesUseCase {

    private final CatalogClientGateway catalog;
    private final EntradaClinicaRepositoryGateway entradas;
    private final Auditoria auditoria;

    public ListarAtencionesUseCaseImpl(CatalogClientGateway catalog, EntradaClinicaRepositoryGateway entradas, Auditoria auditoria) {
        this.catalog = catalog;
        this.entradas = entradas;
        this.auditoria = auditoria;
    }

    @Override
    public PaginaEntradas ejecutar(Actor actor, int pagina, int tamano) {
        try {
            AyudaHistoria.exigirProfesional(actor);
            ReglasHistoria.validarPaginacion(pagina, tamano);
            PaginaEntradas resultado = entradas.listarDeProfesional(AyudaHistoria.profesionalDe(catalog, actor), pagina, tamano);
            // RF-30: cada paciente cuya historia aparece en la pagina queda registrado.
            Set<String> pacientes = new LinkedHashSet<>();
            for (EntradaClinica e : resultado.contenido()) {
                pacientes.add(e.pacienteId());
            }
            for (String pacienteId : pacientes) {
                auditoria.permitido(actor, pacienteId, AccionAcceso.LECTURA, null);
            }
            return resultado;
        } catch (RuntimeException e) {
            auditoria.denegado(actor, null, AccionAcceso.LECTURA, null);
            throw e;
        }
    }
}

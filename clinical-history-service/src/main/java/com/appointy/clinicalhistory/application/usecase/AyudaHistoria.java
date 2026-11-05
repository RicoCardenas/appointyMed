package com.appointy.clinicalhistory.application.usecase;

import com.appointy.clinicalhistory.domain.exception.AccesoProhibidoException;
import com.appointy.clinicalhistory.domain.gateway.CatalogClientGateway;
import com.appointy.clinicalhistory.domain.model.Actor;
import com.appointy.clinicalhistory.domain.model.ProfesionalDeUsuario;

/** Pasos que comparten los casos de uso de historia clinica. */
final class AyudaHistoria {

    private AyudaHistoria() {}

    static void exigirProfesional(Actor actor) {
        if (!actor.esProfesional()) {
            throw new AccesoProhibidoException("SOLO_PROFESIONAL", "Esta operacion es solo para profesionales");
        }
    }

    /** El profesional de catalog que corresponde a la cuenta; 403 si la cuenta no tiene perfil o esta inactiva. */
    static String profesionalDe(CatalogClientGateway catalog, Actor actor) {
        ProfesionalDeUsuario p = catalog.buscarProfesionalDeUsuario(actor.usuarioId());
        if (p == null || !p.activo()) {
            throw new AccesoProhibidoException("SIN_PERFIL_PROFESIONAL", "Tu cuenta no tiene un perfil de profesional activo");
        }
        return p.profesionalId();
    }
}

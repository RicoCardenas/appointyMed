package com.appointy.appointment.application.usecase;

import com.appointy.appointment.domain.exception.AccesoProhibidoException;
import com.appointy.appointment.domain.exception.ConflictoException;
import com.appointy.appointment.domain.exception.NoEncontradoException;
import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.CitaConFranja;
import com.appointy.appointment.domain.model.Franja;
import com.appointy.appointment.domain.model.ProfesionalDeUsuario;
import com.appointy.appointment.domain.port.out.CatalogClientPort;
import com.appointy.appointment.domain.port.out.CitaRepositoryPort;
import com.appointy.appointment.domain.port.out.FranjaRepositoryPort;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Pasos que comparten varios casos de uso de citas. */
final class AyudaCitas {

    private AyudaCitas() {}

    static String nuevoId() {
        return UUID.randomUUID().toString();
    }

    static Cita buscarCita(CitaRepositoryPort citas, String citaId) {
        Cita cita = citas.buscarPorId(citaId);
        if (cita == null) {
            throw new NoEncontradoException("CITA_NO_ENCONTRADA", "La cita no existe");
        }
        return cita;
    }

    static Franja buscarFranja(FranjaRepositoryPort franjas, String franjaId) {
        Franja franja = franjas.buscarPorId(franjaId);
        if (franja == null) {
            throw new NoEncontradoException("FRANJA_NO_ENCONTRADA", "La franja no existe");
        }
        return franja;
    }

    static Franja buscarFranjaParaActualizar(FranjaRepositoryPort franjas, String franjaId) {
        Franja franja = franjas.buscarParaActualizar(franjaId);
        if (franja == null) {
            throw new NoEncontradoException("FRANJA_NO_ENCONTRADA", "La franja no existe");
        }
        return franja;
    }

    /**
     * Se llama con la franja de la cita ya bloqueada. Quien cambia una cita bloquea antes su franja, asi que si la cita se
     * movio o cambio de estado entre la lectura previa y el bloqueo, aqui se nota y se rechaza como conflicto.
     */
    static Cita cargarCitaBajoBloqueo(CitaRepositoryPort citas, String citaId, String franjaEsperadaId) {
        Cita cita = buscarCita(citas, citaId);
        if (!cita.franjaId().equals(franjaEsperadaId)) {
            throw new ConflictoException("CITA_CAMBIO", "La cita cambio mientras se procesaba la solicitud; intenta de nuevo");
        }
        return cita;
    }

    static CitaConFranja[] conFranjas(FranjaRepositoryPort franjas, Cita[] citas) {
        String[] ids = new String[citas.length];
        for (int i = 0; i < citas.length; i++) {
            ids[i] = citas[i].franjaId();
        }
        Map<String, Franja> porId = new HashMap<>();
        for (Franja f : franjas.buscarPorIds(ids)) {
            porId.put(f.id(), f);
        }
        CitaConFranja[] resultado = new CitaConFranja[citas.length];
        for (int i = 0; i < citas.length; i++) {
            resultado[i] = new CitaConFranja(citas[i], porId.get(citas[i].franjaId()));
        }
        return resultado;
    }

    /** El profesional de catalog que corresponde a la cuenta; 403 si la cuenta no tiene perfil o esta inactiva. */
    static String profesionalDe(CatalogClientPort catalog, Actor actor) {
        ProfesionalDeUsuario p = catalog.buscarProfesionalDeUsuario(actor.usuarioId());
        if (p == null || !p.activo()) {
            throw new AccesoProhibidoException("SIN_PERFIL_PROFESIONAL", "Tu cuenta no tiene un perfil de profesional activo");
        }
        return p.profesionalId();
    }

    /** Lectura de una cita: su paciente, su profesional o personal administrativo (RNF-05). */
    static void exigirLectura(CatalogClientPort catalog, Actor actor, Cita cita) {
        if (actor.esAdministrativo()) {
            return;
        }
        if (actor.esPaciente() && cita.pacienteId().equals(actor.usuarioId())) {
            return;
        }
        if (actor.esProfesional() && cita.profesionalId().equals(profesionalDe(catalog, actor))) {
            return;
        }
        throw new AccesoProhibidoException("NO_ES_TU_CITA", "No puedes ver una cita que no es tuya");
    }
}

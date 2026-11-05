package com.appointy.catalog.domain.model;

/** {@code usuarioId} referencia por valor a la cuenta de auth y no cambia nunca. */
public record Profesional(String id, String usuarioId, String nombres, String apellidos, String registroMedico, boolean activo) {

    public Profesional actualizar(String nuevosNombres, String nuevosApellidos, String nuevoRegistroMedico) {
        return new Profesional(id, usuarioId, nuevosNombres, nuevosApellidos, nuevoRegistroMedico, activo);
    }

    public Profesional desactivar() {
        return new Profesional(id, usuarioId, nombres, apellidos, registroMedico, false);
    }
}

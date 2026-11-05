package com.appointy.clinicalhistory.domain.model;

/** Usuario autenticado que pide la operacion. {@code rol}: PACIENTE, PROFESIONAL o ADMINISTRATIVO. */
public record Actor(String usuarioId, String rol) {

    public boolean esPaciente() {
        return "PACIENTE".equals(rol);
    }

    public boolean esProfesional() {
        return "PROFESIONAL".equals(rol);
    }

    public boolean esAdministrativo() {
        return "ADMINISTRATIVO".equals(rol);
    }
}

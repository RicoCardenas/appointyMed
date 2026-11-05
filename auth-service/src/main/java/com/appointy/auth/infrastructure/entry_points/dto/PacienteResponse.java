package com.appointy.auth.infrastructure.entry_points.dto;

import com.appointy.auth.domain.model.PacienteConCuenta;

public record PacienteResponse(
        String usuarioId,
        String correo,
        String cedula,
        String nombres,
        String apellidos,
        String fechaNacimiento,
        String telefono,
        String direccion,
        String ciudad) {

    public static PacienteResponse de(PacienteConCuenta p) {
        return new PacienteResponse(
                p.usuario().id(),
                p.usuario().correo(),
                p.perfil().cedula(),
                p.perfil().nombres(),
                p.perfil().apellidos(),
                p.perfil().fechaNacimiento(),
                p.perfil().telefono(),
                p.perfil().direccion(),
                p.perfil().ciudad());
    }
}

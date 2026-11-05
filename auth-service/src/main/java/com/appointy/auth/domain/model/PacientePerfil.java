package com.appointy.auth.domain.model;

/**
 * Perfil del paciente (RF-04 a RF-06). {@code fechaNacimiento} es texto ISO yyyy-MM-dd.
 * Cedula, nombres, apellidos y fecha de nacimiento son inmutables para el paciente: solo
 * {@link #corregirIdentidad} los cambia y solo lo invoca el proceso administrativo. El paciente
 * unicamente puede usar {@link #actualizarContacto}.
 */
public record PacientePerfil(
        String usuarioId,
        String cedula,
        String nombres,
        String apellidos,
        String fechaNacimiento,
        String telefono,
        String direccion,
        String ciudad) {

    /** Los valores nulos se interpretan como "sin cambio". */
    public PacientePerfil actualizarContacto(String nuevoTelefono, String nuevaDireccion, String nuevaCiudad) {
        return new PacientePerfil(
                usuarioId,
                cedula,
                nombres,
                apellidos,
                fechaNacimiento,
                nuevoTelefono != null ? nuevoTelefono : telefono,
                nuevaDireccion != null ? nuevaDireccion : direccion,
                nuevaCiudad != null ? nuevaCiudad : ciudad);
    }

    /** Los valores nulos se interpretan como "sin cambio". */
    public PacientePerfil corregirIdentidad(
            String nuevaCedula, String nuevosNombres, String nuevosApellidos, String nuevaFechaNacimiento) {
        return new PacientePerfil(
                usuarioId,
                nuevaCedula != null ? nuevaCedula : cedula,
                nuevosNombres != null ? nuevosNombres : nombres,
                nuevosApellidos != null ? nuevosApellidos : apellidos,
                nuevaFechaNacimiento != null ? nuevaFechaNacimiento : fechaNacimiento,
                telefono,
                direccion,
                ciudad);
    }
}

package com.appointy.auth.domain.model;

/** Solo el nombre: sin cedula, contacto ni fecha de nacimiento (RNF-05). */
public record NombrePaciente(String usuarioId, String nombres, String apellidos) {}

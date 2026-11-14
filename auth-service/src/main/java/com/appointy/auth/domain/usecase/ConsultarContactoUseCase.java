package com.appointy.auth.domain.usecase;

/** Consulta interna para notification (CU-33): el correo de una cuenta activa. */
public interface ConsultarContactoUseCase {

    /** Devuelve el correo del usuario. Lanza NoEncontradoException si no existe o esta inactivo. */
    String ejecutar(String usuarioId);
}

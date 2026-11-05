package com.appointy.auth.domain.usecase;

/** Crea el primer ADMINISTRATIVO si no existe ninguno; ninguna ruta publica puede hacerlo. */
public interface SembrarAdministradorUseCase {

    /** Devuelve true si creo la cuenta. */
    boolean ejecutar(Comando comando);

    record Comando(String correo, String clave) {}
}

package com.appointy.auth.domain.exception;

/** Correo o clave incorrectos, o cuenta inactiva. El mensaje es unico para no revelar cual fallo (HTTP 401). */
public class CredencialesInvalidasException extends DomainException {

    public CredencialesInvalidasException() {
        super("CREDENCIALES_INVALIDAS", "Correo o clave incorrectos");
    }
}

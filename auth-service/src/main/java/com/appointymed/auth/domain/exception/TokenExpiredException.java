package com.appointymed.auth.domain.exception;

public class TokenExpiredException extends DomainException {
    public TokenExpiredException() {
        super("Token has expired", "TOKEN_EXPIRED");
    }

    public TokenExpiredException(String message) {
        super(message, "TOKEN_EXPIRED");
    }
}

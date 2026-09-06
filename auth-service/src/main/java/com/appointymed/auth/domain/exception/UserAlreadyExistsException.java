package com.appointymed.auth.domain.exception;

public class UserAlreadyExistsException extends DomainException {
    public UserAlreadyExistsException(String email) {
        super("User already exists with email: " + email, "USER_ALREADY_EXISTS");
    }
}

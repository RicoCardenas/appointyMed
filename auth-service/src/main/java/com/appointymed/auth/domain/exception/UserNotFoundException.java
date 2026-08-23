package com.appointymed.auth.domain.exception;

import java.util.UUID;

public class UserNotFoundException extends DomainException {
    public UserNotFoundException(UUID userId) {
        super("User not found with id: " + userId, "USER_NOT_FOUND");
    }

    public UserNotFoundException(String email) {
        super("User not found with email: " + email, "USER_NOT_FOUND");
    }
}
package com.appointymed.auth.domain.exception;

import com.appointymed.auth.domain.model.DomainId;

public class UserNotFoundException extends DomainException {
    public UserNotFoundException(DomainId userId) {
        super("User not found with id: " + userId.value(), "USER_NOT_FOUND");
    }

    public UserNotFoundException(String email) {
        super("User not found with email: " + email, "USER_NOT_FOUND");
    }
}

package com.appointymed.appointment.domain.exception;

import java.util.UUID;

public class ResourceNotFoundException extends DomainException {
    public ResourceNotFoundException(String resourceName, UUID id) {
        super(resourceName + " with ID '" + id + "' was not found", "RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String resourceName, String identifier) {
        super(resourceName + " with identifier '" + identifier + "' was not found", "RESOURCE_NOT_FOUND");
    }
}

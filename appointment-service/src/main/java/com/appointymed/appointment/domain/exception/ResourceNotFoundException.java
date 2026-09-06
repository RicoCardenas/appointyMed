package com.appointymed.appointment.domain.exception;

import com.appointymed.appointment.domain.model.DomainId;

public final class ResourceNotFoundException extends DomainException {
    public ResourceNotFoundException(String resourceName, DomainId id) {
        super(resourceName + " with ID '" + id + "' was not found", "RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String resourceName, String identifier) {
        super(
                resourceName + " with identifier '" + identifier + "' was not found", "RESOURCE_NOT_FOUND");
    }
}

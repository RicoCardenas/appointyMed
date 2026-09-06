package com.appointymed.appointment.domain.model;

public final class DomainId {
    private final String value;

    private DomainId(String value) {
        this.value = value;
    }

    public static DomainId of(String value) {
        if (value == null || value.trim().isEmpty())
            throw new IllegalArgumentException("ID cannot be null or blank");
        return new DomainId(value.trim());
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof DomainId && value.equals(((DomainId) other).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}

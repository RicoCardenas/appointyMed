package com.appointymed.auth.domain.model;

public final class Email {
    private final String value;

    private Email(String value) {
        this.value = value;
    }

    public static Email of(String value) {
        if (value == null || value.trim().isEmpty())
            throw new IllegalArgumentException("Email cannot be null or empty");
        String normalized = value.trim().toLowerCase();
        int at = normalized.indexOf('@');
        if (at <= 0
                || at != normalized.lastIndexOf('@')
                || at == normalized.length() - 1
                || normalized.indexOf('.', at + 1) < 0
                || normalized.indexOf(' ') >= 0) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
        if (normalized.length() > 255)
            throw new IllegalArgumentException("Email cannot exceed 255 characters");
        return new Email(normalized);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Email && value.equals(((Email) other).value);
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

package com.appointymed.auth.domain.model;

public final class Password {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;
    private final String value;

    private Password(String value) {
        this.value = value;
    }

    public static Password ofRaw(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty())
            throw new IllegalArgumentException("Password cannot be null or empty");
        if (rawPassword.length() < MIN_LENGTH)
            throw new IllegalArgumentException("Password must be at least " + MIN_LENGTH + " characters");
        if (rawPassword.length() > MAX_LENGTH)
            throw new IllegalArgumentException("Password cannot exceed " + MAX_LENGTH + " characters");
        boolean upper = false, lower = false, digit = false, special = false;
        for (int index = 0; index < rawPassword.length(); index++) {
            char character = rawPassword.charAt(index);
            upper = upper || character >= 'A' && character <= 'Z';
            lower = lower || character >= 'a' && character <= 'z';
            digit = digit || character >= '0' && character <= '9';
            special = special || "!@#$%^&*()_+-=[]{};':\\\"|,.<>/?".indexOf(character) >= 0;
        }
        if (!upper)
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        if (!lower)
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        if (!digit) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
        if (!special)
            throw new IllegalArgumentException("Password must contain at least one special character");
        return new Password(rawPassword);
    }

    public static Password ofHashed(String hashedValue) {
        if (hashedValue == null || hashedValue.isEmpty())
            throw new IllegalArgumentException("Hashed password cannot be null or empty");
        return new Password(hashedValue);
    }

    public String getHashedValue() {
        return value;
    }

    public boolean matches(String rawPassword, PasswordEncoder encoder) {
        return encoder.matches(rawPassword, value);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Password && value.equals(((Password) other).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public interface PasswordEncoder {
        String encode(String rawPassword);

        boolean matches(String rawPassword, String hashedPassword);
    }
}

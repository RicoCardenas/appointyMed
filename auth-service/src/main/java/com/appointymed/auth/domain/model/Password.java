package com.appointymed.auth.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

public final class Password {
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

    private final String hashedValue;

    private Password(String hashedValue) {
        this.hashedValue = hashedValue;
    }

    public static Password ofRaw(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (rawPassword.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_LENGTH + " characters");
        }
        if (rawPassword.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Password cannot exceed " + MAX_LENGTH + " characters");
        }
        if (!UPPERCASE_PATTERN.matcher(rawPassword).find()) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }
        if (!LOWERCASE_PATTERN.matcher(rawPassword).find()) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }
        if (!DIGIT_PATTERN.matcher(rawPassword).find()) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
        if (!SPECIAL_CHAR_PATTERN.matcher(rawPassword).find()) {
            throw new IllegalArgumentException("Password must contain at least one special character");
        }
        return new Password(rawPassword);
    }

    public static Password ofHashed(String hashedValue) {
        if (hashedValue == null || hashedValue.isEmpty()) {
            throw new IllegalArgumentException("Hashed password cannot be null or empty");
        }
        return new Password(hashedValue);
    }

    public String getHashedValue() {
        return hashedValue;
    }

    public boolean matches(String rawPassword, PasswordEncoder encoder) {
        return encoder.matches(rawPassword, this.hashedValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Password password = (Password) o;
        return Objects.equals(hashedValue, password.hashedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashedValue);
    }

    public interface PasswordEncoder {
        String encode(String rawPassword);
        boolean matches(String rawPassword, String hashedPassword);
    }
}
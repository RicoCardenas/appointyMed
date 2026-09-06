package com.appointymed.auth.domain.model;

public final class PhoneNumber {
    private final String value;

    private PhoneNumber(String value) {
        this.value = value;
    }

    public static PhoneNumber of(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        String cleaned =
                value.trim().replace(" ", "").replace("-", "").replace("(", "").replace(")", "");
        int start = cleaned.charAt(0) == '+' ? 1 : 0;
        if (start == cleaned.length() || cleaned.length() > 20)
            throw new IllegalArgumentException("Invalid phone number format: " + value);
        for (int index = start; index < cleaned.length(); index++) {
            if (cleaned.charAt(index) < '0' || cleaned.charAt(index) > '9')
                throw new IllegalArgumentException("Invalid phone number format: " + value);
        }
        return new PhoneNumber(cleaned);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof PhoneNumber && value.equals(((PhoneNumber) other).value);
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

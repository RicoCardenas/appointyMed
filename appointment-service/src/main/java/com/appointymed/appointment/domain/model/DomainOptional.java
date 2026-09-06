package com.appointymed.appointment.domain.model;

public final class DomainOptional<T> {
    private final T value;

    private DomainOptional(T value) {
        this.value = value;
    }

    public static <T> DomainOptional<T> of(T value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        return new DomainOptional<>(value);
    }

    public static <T> DomainOptional<T> empty() {
        return new DomainOptional<>(null);
    }

    public boolean isPresent() {
        return value != null;
    }

    public boolean isEmpty() {
        return value == null;
    }

    public T get() {
        if (value == null) {
            throw new IllegalStateException("Value not present");
        }
        return value;
    }

    public T orElse(T fallback) {
        return value == null ? fallback : value;
    }

    public T orElseThrow(DomainSupplier<? extends RuntimeException> supplier) {
        if (value == null) {
            throw supplier.get();
        }
        return value;
    }

    public interface DomainSupplier<T> {
        T get();
    }
}

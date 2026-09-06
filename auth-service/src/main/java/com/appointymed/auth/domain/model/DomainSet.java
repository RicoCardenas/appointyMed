package com.appointymed.auth.domain.model;

public final class DomainSet<T> {
    private final Object[] values;

    private DomainSet(Object[] values) {
        this.values = values;
    }

    public static <T> DomainSet<T> empty() {
        return new DomainSet<>(new Object[0]);
    }

    @SafeVarargs
    public static <T> DomainSet<T> of(T... values) {
        Object[] copy = new Object[values.length];
        for (int index = 0; index < values.length; index++) {
            copy[index] = values[index];
        }
        return new DomainSet<>(copy);
    }

    public boolean contains(T value) {
        for (Object item : values) {
            if (item == value || (item != null && item.equals(value))) {
                return true;
            }
        }
        return false;
    }

    public int size() {
        return values.length;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        return (T) values[index];
    }

    public DomainSet<T> add(T value) {
        if (contains(value)) {
            return this;
        }
        Object[] next = copy(values, values.length + 1);
        next[values.length] = value;
        return new DomainSet<>(next);
    }

    public DomainSet<T> remove(T value) {
        int index = -1;
        for (int position = 0; position < values.length; position++) {
            if (values[position] == value
                    || (values[position] != null && values[position].equals(value))) {
                index = position;
                break;
            }
        }
        if (index < 0) {
            return this;
        }
        Object[] next = new Object[values.length - 1];
        for (int source = 0, target = 0; source < values.length; source++) {
            if (source != index) {
                next[target++] = values[source];
            }
        }
        return new DomainSet<>(next);
    }

    public boolean any(DomainPredicate<T> predicate) {
        for (Object value : values) {
            if (predicate.test(cast(value))) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private T cast(Object value) {
        return (T) value;
    }

    private static Object[] copy(Object[] source, int length) {
        Object[] result = new Object[length];
        for (int index = 0; index < source.length && index < length; index++) {
            result[index] = source[index];
        }
        return result;
    }

    public interface DomainPredicate<T> {
        boolean test(T value);
    }
}

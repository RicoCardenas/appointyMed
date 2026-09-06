package com.appointymed.appointment.domain.model;

public final class DomainList<T> {
    private Object[] values = new Object[0];

    public void add(T value) {
        Object[] next = new Object[values.length + 1];
        for (int i = 0; i < values.length; i++) {
            next[i] = values[i];
        }
        next[values.length] = value;
        values = next;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        return (T) values[index];
    }

    public int size() {
        return values.length;
    }

    public boolean isEmpty() {
        return values.length == 0;
    }
}

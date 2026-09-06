package com.appointymed.auth.domain.model;

public final class DomainList<T> {
    private Object[] values;

    public DomainList() {
        values = new Object[0];
    }

    public void add(T value) {
        Object[] next = new Object[values.length + 1];
        for (int index = 0; index < values.length; index++) {
            next[index] = values[index];
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

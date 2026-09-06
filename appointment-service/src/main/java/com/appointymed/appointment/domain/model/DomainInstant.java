package com.appointymed.appointment.domain.model;

public final class DomainInstant {
    private final long epochMillis;

    private DomainInstant(long value) {
        epochMillis = value;
    }

    public static DomainInstant ofEpochMillis(long value) {
        return new DomainInstant(value);
    }

    public long toEpochMillis() {
        return epochMillis;
    }

    public boolean isBefore(DomainInstant other) {
        return epochMillis < other.epochMillis;
    }

    public boolean isAfter(DomainInstant other) {
        return epochMillis > other.epochMillis;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof DomainInstant && epochMillis == ((DomainInstant) other).epochMillis;
    }

    @Override
    public int hashCode() {
        return (int) (epochMillis ^ (epochMillis >>> 32));
    }
}

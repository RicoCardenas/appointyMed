package com.appointymed.appointment.domain.model;

public final class DomainTime {
    public static final DomainTime MAX = new DomainTime(1439);
    private final int minuteOfDay;

    private DomainTime(int value) {
        minuteOfDay = value;
    }

    public static DomainTime ofMinuteOfDay(int value) {
        if (value < 0 || value > 1439) {
            throw new IllegalArgumentException("Invalid time");
        }
        return new DomainTime(value);
    }

    public int toMinuteOfDay() {
        return minuteOfDay;
    }

    public DomainTime plusMinutes(int value) {
        return ofMinuteOfDay(minuteOfDay + value);
    }

    public boolean isBefore(DomainTime other) {
        return minuteOfDay < other.minuteOfDay;
    }

    public boolean isAfter(DomainTime other) {
        return minuteOfDay > other.minuteOfDay;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof DomainTime && minuteOfDay == ((DomainTime) other).minuteOfDay;
    }

    @Override
    public int hashCode() {
        return minuteOfDay;
    }

    @Override
    public String toString() {
        return (minuteOfDay / 60 < 10 ? "0" : "")
                + (minuteOfDay / 60)
                + ":"
                + (minuteOfDay % 60 < 10 ? "0" : "")
                + (minuteOfDay % 60);
    }
}

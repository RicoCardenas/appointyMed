package com.appointymed.appointment.domain.model;

public final class DomainDateTime {
    private static final long MILLIS_PER_DAY = 86400000L;
    private final long epochMillis;

    private DomainDateTime(long value) {
        epochMillis = value;
    }

    public static DomainDateTime ofEpochMillis(long value) {
        return new DomainDateTime(value);
    }

    public long toEpochMillis() {
        return epochMillis;
    }

    public boolean isBefore(DomainDateTime other) {
        return epochMillis < other.epochMillis;
    }

    public boolean isAfter(DomainDateTime other) {
        return epochMillis > other.epochMillis;
    }

    public boolean isEqual(DomainDateTime other) {
        return epochMillis == other.epochMillis;
    }

    public int getDayOfWeek() {
        long day = floorDay();
        long normalized = ((day + 3) % 7 + 7) % 7;
        return (int) normalized + 1;
    }

    public DomainDateTime plusMinutes(int minutes) {
        return ofEpochMillis(epochMillis + minutes * 60000L);
    }

    public DomainTime toLocalTime() {
        long minutes = (epochMillis - floorDay() * MILLIS_PER_DAY) / 60000L;
        return DomainTime.ofMinuteOfDay((int) minutes);
    }

    private long floorDay() {
        long day = epochMillis / MILLIS_PER_DAY;
        return epochMillis >= 0 || epochMillis % MILLIS_PER_DAY == 0 ? day : day - 1;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof DomainDateTime && epochMillis == ((DomainDateTime) other).epochMillis;
    }

    @Override
    public int hashCode() {
        return (int) (epochMillis ^ (epochMillis >>> 32));
    }
}

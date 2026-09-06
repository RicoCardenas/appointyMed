package com.appointymed.appointment.domain.model;

public final class DomainDate {
    private static final long MILLIS_PER_DAY = 86400000L;
    private final long epochDay;

    private DomainDate(long value) {
        epochDay = value;
    }

    public static DomainDate ofEpochDay(long value) {
        return new DomainDate(value);
    }

    public long toEpochDay() {
        return epochDay;
    }

    public int getDayOfWeek() {
        long normalized = ((epochDay + 3) % 7 + 7) % 7;
        return (int) normalized + 1;
    }

    public DomainDateTime atStartOfDay() {
        return DomainDateTime.ofEpochMillis(epochDay * MILLIS_PER_DAY);
    }

    public DomainDateTime atTime(DomainTime time) {
        return DomainDateTime.ofEpochMillis(epochDay * MILLIS_PER_DAY + time.toMinuteOfDay() * 60000L);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof DomainDate && epochDay == ((DomainDate) other).epochDay;
    }

    @Override
    public int hashCode() {
        return (int) (epochDay ^ (epochDay >>> 32));
    }
}

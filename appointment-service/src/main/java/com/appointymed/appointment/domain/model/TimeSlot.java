package com.appointymed.appointment.domain.model;

public final class TimeSlot {
    private final DomainDateTime startDateTime, endDateTime;
    private final boolean available;

    public TimeSlot(DomainDateTime start, DomainDateTime end, boolean available) {
        startDateTime = start;
        endDateTime = end;
        this.available = available;
    }

    public DomainDateTime startDateTime() {
        return startDateTime;
    }

    public DomainDateTime endDateTime() {
        return endDateTime;
    }

    public boolean available() {
        return available;
    }

    public static TimeSlot available(DomainDateTime start, DomainDateTime end) {
        return new TimeSlot(start, end, true);
    }

    public static TimeSlot occupied(DomainDateTime start, DomainDateTime end) {
        return new TimeSlot(start, end, false);
    }
}

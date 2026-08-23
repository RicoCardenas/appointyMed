package com.appointymed.appointment.domain.model;

import java.time.LocalDateTime;

public record TimeSlot(
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        boolean available
) {
    public static TimeSlot available(LocalDateTime start, LocalDateTime end) {
        return new TimeSlot(start, end, true);
    }

    public static TimeSlot occupied(LocalDateTime start, LocalDateTime end) {
        return new TimeSlot(start, end, false);
    }
}

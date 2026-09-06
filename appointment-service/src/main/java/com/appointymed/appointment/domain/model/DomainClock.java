package com.appointymed.appointment.domain.model;

public interface DomainClock {
    DomainDateTime now();

    default DomainInstant asInstant() {
        return DomainInstant.ofEpochMillis(now().toEpochMillis());
    }
}

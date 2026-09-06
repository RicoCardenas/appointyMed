package com.appointymed.appointment.infrastructure.persistence.mapper;

import com.appointymed.appointment.domain.model.DomainDate;
import com.appointymed.appointment.domain.model.DomainDateTime;
import com.appointymed.appointment.domain.model.DomainId;
import com.appointymed.appointment.domain.model.DomainInstant;
import com.appointymed.appointment.domain.model.DomainTime;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.UUID;

public final class AppointmentDomainValueMapper {
    private AppointmentDomainValueMapper() {}

    public static DomainId toDomainId(UUID value) { return value == null ? null : DomainId.of(value.toString()); }
    public static UUID toUuid(DomainId value) { return value == null ? null : UUID.fromString(value.value()); }
    public static DomainInstant toDomainInstant(Instant value) { return value == null ? null : DomainInstant.ofEpochMillis(value.toEpochMilli()); }
    public static Instant toInstant(DomainInstant value) { return value == null ? null : Instant.ofEpochMilli(value.toEpochMillis()); }
    public static DomainDate toDomainDate(LocalDate value) { return value == null ? null : DomainDate.ofEpochDay(value.toEpochDay()); }
    public static LocalDate toLocalDate(DomainDate value) { return value == null ? null : LocalDate.ofEpochDay(value.toEpochDay()); }
    public static DomainDateTime toDomainDateTime(LocalDateTime value) {
        return value == null ? null : DomainDateTime.ofEpochMillis(value.toInstant(ZoneOffset.UTC).toEpochMilli());
    }
    public static LocalDateTime toLocalDateTime(DomainDateTime value) {
        return value == null ? null : LocalDateTime.ofInstant(Instant.ofEpochMilli(value.toEpochMillis()), ZoneOffset.UTC);
    }
    public static DomainTime toDomainTime(LocalTime value) {
        return value == null ? null : DomainTime.ofMinuteOfDay(value.getHour() * 60 + value.getMinute());
    }
    public static LocalTime toLocalTime(DomainTime value) {
        return value == null ? null : LocalTime.of(value.toMinuteOfDay() / 60, value.toMinuteOfDay() % 60);
    }
}

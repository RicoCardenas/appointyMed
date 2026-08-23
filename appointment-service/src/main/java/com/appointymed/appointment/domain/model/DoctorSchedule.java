package com.appointymed.appointment.domain.model;

import java.time.LocalTime;
import java.util.UUID;

public class DoctorSchedule {
    private final UUID id;
    private final UUID doctorId;
    private final int dayOfWeek; // 1: Monday, 7: Sunday
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final int slotDurationMinutes;
    private final boolean active;

    public DoctorSchedule(UUID id, UUID doctorId, int dayOfWeek, LocalTime startTime, LocalTime endTime, int slotDurationMinutes, boolean active) {
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new IllegalArgumentException("Day of week must be between 1 (Monday) and 7 (Sunday)");
        }
        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            throw new IllegalArgumentException("End time must be strictly after start time");
        }
        this.id = id != null ? id : UUID.randomUUID();
        this.doctorId = doctorId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotDurationMinutes = slotDurationMinutes > 0 ? slotDurationMinutes : 30;
        this.active = active;
    }

    public UUID getId() {
        return id;
    }

    public UUID getDoctorId() {
        return doctorId;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public int getSlotDurationMinutes() {
        return slotDurationMinutes;
    }

    public boolean isActive() {
        return active;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID doctorId;
        private int dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
        private int slotDurationMinutes = 30;
        private boolean active = true;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder doctorId(UUID doctorId) {
            this.doctorId = doctorId;
            return this;
        }

        public Builder dayOfWeek(int dayOfWeek) {
            this.dayOfWeek = dayOfWeek;
            return this;
        }

        public Builder startTime(LocalTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(LocalTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder slotDurationMinutes(int slotDurationMinutes) {
            this.slotDurationMinutes = slotDurationMinutes;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public DoctorSchedule build() {
            return new DoctorSchedule(id, doctorId, dayOfWeek, startTime, endTime, slotDurationMinutes, active);
        }
    }
}

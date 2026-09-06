package com.appointymed.appointment.domain.model;

public final class DoctorSchedule {
    private final DomainId id, doctorId;
    private final int dayOfWeek;
    private final DomainTime startTime, endTime;
    private final int slotDurationMinutes;
    private final boolean active;

    public DoctorSchedule(
            DomainId id,
            DomainId doctorId,
            int dayOfWeek,
            DomainTime startTime,
            DomainTime endTime,
            int duration,
            boolean active) {
        if (dayOfWeek < 1 || dayOfWeek > 7)
            throw new IllegalArgumentException("Day of week must be between 1 (Monday) and 7 (Sunday)");
        if (startTime == null || endTime == null || !endTime.isAfter(startTime))
            throw new IllegalArgumentException("End time must be strictly after start time");
        if (duration <= 0) {
            throw new IllegalArgumentException("Slot duration must be positive");
        }
        this.id = id;
        this.doctorId = doctorId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        slotDurationMinutes = duration;
        this.active = active;
    }

    public DomainId getId() {
        return id;
    }

    public DomainId getDoctorId() {
        return doctorId;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public DomainTime getStartTime() {
        return startTime;
    }

    public DomainTime getEndTime() {
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

    public static final class Builder {
        private DomainId id, doctorId;
        private int day;
        private DomainTime start, end;
        private int duration = 30;
        private boolean active = true;

        public Builder id(DomainId v) {
            id = v;
            return this;
        }

        public Builder doctorId(DomainId v) {
            doctorId = v;
            return this;
        }

        public Builder dayOfWeek(int v) {
            day = v;
            return this;
        }

        public Builder startTime(DomainTime v) {
            start = v;
            return this;
        }

        public Builder endTime(DomainTime v) {
            end = v;
            return this;
        }

        public Builder slotDurationMinutes(int v) {
            duration = v;
            return this;
        }

        public Builder active(boolean v) {
            active = v;
            return this;
        }

        public DoctorSchedule build() {
            return new DoctorSchedule(id, doctorId, day, start, end, duration, active);
        }
    }
}

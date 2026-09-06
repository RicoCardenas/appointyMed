package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.exception.ResourceNotFoundException;
import com.appointymed.appointment.domain.model.Appointment;
import com.appointymed.appointment.domain.model.Doctor;
import com.appointymed.appointment.domain.model.DoctorSchedule;
import com.appointymed.appointment.domain.model.DomainDate;
import com.appointymed.appointment.domain.model.DomainDateTime;
import com.appointymed.appointment.domain.model.DomainId;
import com.appointymed.appointment.domain.model.DomainList;
import com.appointymed.appointment.domain.model.DomainTime;
import com.appointymed.appointment.domain.model.TimeSlot;
import com.appointymed.appointment.domain.repository.AppointmentRepository;
import com.appointymed.appointment.domain.repository.DoctorRepository;
import com.appointymed.appointment.domain.repository.DoctorScheduleRepository;

public final class GetDoctorAvailabilityUseCase {
    private final DoctorRepository doctors;
    private final DoctorScheduleRepository schedules;
    private final AppointmentRepository appointments;

    public GetDoctorAvailabilityUseCase(
            DoctorRepository d, DoctorScheduleRepository s, AppointmentRepository a) {
        doctors = d;
        schedules = s;
        appointments = a;
    }

    public DomainList<TimeSlot> execute(DomainId id, DomainDate date) {
        Doctor doctor =
                doctors.findById(id).orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
        var optional = schedules.findByDoctorIdAndDayOfWeek(doctor.getId(), date.getDayOfWeek());
        if (optional.isEmpty() || !optional.get().isActive()) {
            return new DomainList<>();
        }
        DoctorSchedule schedule = optional.get();
        DomainList<Appointment> existing =
                appointments.findActiveAppointmentsByDoctorAndDate(
                        id, date.atStartOfDay(), date.atTime(DomainTime.MAX));
        DomainList<TimeSlot> result = new DomainList<>();
        DomainTime current = schedule.getStartTime();
        while (current.plusMinutes(schedule.getSlotDurationMinutes()).isBefore(schedule.getEndTime())
                || current.plusMinutes(schedule.getSlotDurationMinutes()).equals(schedule.getEndTime())) {
            DomainDateTime start = date.atTime(current),
                    end = start.plusMinutes(schedule.getSlotDurationMinutes());
            boolean occupied = false;
            for (int i = 0; i < existing.size(); i++) {
                Appointment a = existing.get(i);
                if (start.isBefore(a.getEndDateTime()) && end.isAfter(a.getAppointmentDateTime())) {
                    occupied = true;
                    break;
                }
            }
            result.add(new TimeSlot(start, end, !occupied));
            current = current.plusMinutes(schedule.getSlotDurationMinutes());
        }
        return result;
    }
}

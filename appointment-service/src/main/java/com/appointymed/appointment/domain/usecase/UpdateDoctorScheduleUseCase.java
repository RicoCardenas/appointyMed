package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.exception.ResourceNotFoundException;
import com.appointymed.appointment.domain.model.Doctor;
import com.appointymed.appointment.domain.model.DoctorSchedule;
import com.appointymed.appointment.domain.model.DomainId;
import com.appointymed.appointment.domain.model.DomainList;
import com.appointymed.appointment.domain.repository.DoctorRepository;
import com.appointymed.appointment.domain.repository.DoctorScheduleRepository;

public final class UpdateDoctorScheduleUseCase {
    private final DoctorRepository doctors;
    private final DoctorScheduleRepository schedules;

    public UpdateDoctorScheduleUseCase(DoctorRepository d, DoctorScheduleRepository s) {
        doctors = d;
        schedules = s;
    }

    public DomainList<DoctorSchedule> execute(DomainId id, DomainList<DayScheduleItem> items) {
        Doctor doctor =
                doctors.findById(id).orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
        schedules.deleteByDoctorId(doctor.getId());
        DomainList<DoctorSchedule> result = new DomainList<>();
        for (int i = 0; i < items.size(); i++) {
            DayScheduleItem item = items.get(i);
            result.add(
                    DoctorSchedule.builder()
                            .doctorId(doctor.getId())
                            .dayOfWeek(item.dayOfWeek())
                            .startTime(item.startTime())
                            .endTime(item.endTime())
                            .slotDurationMinutes(item.slotDurationMinutes())
                            .active(item.active())
                            .build());
        }
        return schedules.saveAll(result);
    }

    public record DayScheduleItem(
            int dayOfWeek,
            com.appointymed.appointment.domain.model.DomainTime startTime,
            com.appointymed.appointment.domain.model.DomainTime endTime,
            int slotDurationMinutes,
            boolean active) {}
}

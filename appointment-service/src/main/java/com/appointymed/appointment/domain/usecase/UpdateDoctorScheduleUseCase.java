package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.exception.ResourceNotFoundException;
import com.appointymed.appointment.domain.model.Doctor;
import com.appointymed.appointment.domain.model.DoctorSchedule;
import com.appointymed.appointment.domain.repository.DoctorRepository;
import com.appointymed.appointment.domain.repository.DoctorScheduleRepository;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class UpdateDoctorScheduleUseCase {
    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;

    public UpdateDoctorScheduleUseCase(DoctorRepository doctorRepository, DoctorScheduleRepository doctorScheduleRepository) {
        this.doctorRepository = doctorRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
    }

    public List<DoctorSchedule> execute(UUID doctorId, List<DayScheduleItem> scheduleItems) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        doctorScheduleRepository.deleteByDoctorId(doctor.getId());

        List<DoctorSchedule> schedules = new ArrayList<>();
        for (DayScheduleItem item : scheduleItems) {
            DoctorSchedule schedule = DoctorSchedule.builder()
                    .id(UUID.randomUUID())
                    .doctorId(doctor.getId())
                    .dayOfWeek(item.dayOfWeek())
                    .startTime(item.startTime())
                    .endTime(item.endTime())
                    .slotDurationMinutes(item.slotDurationMinutes() > 0 ? item.slotDurationMinutes() : 30)
                    .active(item.active())
                    .build();
            schedules.add(schedule);
        }

        return doctorScheduleRepository.saveAll(schedules);
    }

    public record DayScheduleItem(
            int dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            int slotDurationMinutes,
            boolean active
    ) {}
}

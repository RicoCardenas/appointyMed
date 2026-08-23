package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.exception.ResourceNotFoundException;
import com.appointymed.appointment.domain.model.Appointment;
import com.appointymed.appointment.domain.model.Doctor;
import com.appointymed.appointment.domain.model.DoctorSchedule;
import com.appointymed.appointment.domain.model.TimeSlot;
import com.appointymed.appointment.domain.repository.AppointmentRepository;
import com.appointymed.appointment.domain.repository.DoctorRepository;
import com.appointymed.appointment.domain.repository.DoctorScheduleRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class GetDoctorAvailabilityUseCase {
    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final AppointmentRepository appointmentRepository;

    public GetDoctorAvailabilityUseCase(DoctorRepository doctorRepository,
                                        DoctorScheduleRepository doctorScheduleRepository,
                                        AppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public List<TimeSlot> execute(UUID doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        int dayOfWeek = date.getDayOfWeek().getValue(); // 1 = Monday ... 7 = Sunday
        Optional<DoctorSchedule> scheduleOpt = doctorScheduleRepository.findByDoctorIdAndDayOfWeek(doctor.getId(), dayOfWeek);

        if (scheduleOpt.isEmpty() || !scheduleOpt.get().isActive()) {
            return Collections.emptyList();
        }

        DoctorSchedule schedule = scheduleOpt.get();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Appointment> existingAppointments = appointmentRepository.findActiveAppointmentsByDoctorAndDate(
                doctor.getId(), startOfDay, endOfDay
        );

        List<TimeSlot> slots = new ArrayList<>();
        LocalTime currentSlotTime = schedule.getStartTime();
        int slotDuration = schedule.getSlotDurationMinutes();

        while (currentSlotTime.plusMinutes(slotDuration).isBefore(schedule.getEndTime()) 
                || currentSlotTime.plusMinutes(slotDuration).equals(schedule.getEndTime())) {
            
            LocalDateTime slotStart = date.atTime(currentSlotTime);
            LocalDateTime slotEnd = slotStart.plusMinutes(slotDuration);

            boolean isOccupied = existingAppointments.stream().anyMatch(app -> 
                    (slotStart.isBefore(app.getEndDateTime()) && slotEnd.isAfter(app.getAppointmentDateTime()))
            );

            slots.add(new TimeSlot(slotStart, slotEnd, !isOccupied));
            currentSlotTime = currentSlotTime.plusMinutes(slotDuration);
        }

        return slots;
    }
}

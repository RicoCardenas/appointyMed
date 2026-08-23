package com.appointymed.appointment.infrastructure.controller;

import com.appointymed.appointment.AppointmentServiceApplication;
import com.appointymed.appointment.application.dto.CreateSpecialtyRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = AppointmentServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AppointmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateAndListSpecialties() throws Exception {
        CreateSpecialtyRequest request = new CreateSpecialtyRequest(
                "Neurología", "Diagnóstico y tratamiento de trastornos del sistema nervioso", 40
        );

        mockMvc.perform(post("/api/v1/specialties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Neurología"))
                .andExpect(jsonPath("$.consultationDurationMinutes").value(40));

        mockMvc.perform(get("/api/v1/specialties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}

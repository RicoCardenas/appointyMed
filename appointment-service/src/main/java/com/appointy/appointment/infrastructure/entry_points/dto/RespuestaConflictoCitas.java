package com.appointy.appointment.infrastructure.entry_points.dto;

import java.time.Instant;
import java.util.List;

/** 409 de CU-26: la estructura uniforme de error mas la lista que catalog espera en {@code citasAfectadas}. */
public record RespuestaConflictoCitas(String codigo, String mensaje, List<String> detalle, Instant timestamp, List<String> citasAfectadas) {}

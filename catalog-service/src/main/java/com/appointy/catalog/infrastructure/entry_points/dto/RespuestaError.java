package com.appointy.catalog.infrastructure.entry_points.dto;

import java.time.Instant;
import java.util.List;

/** Estructura uniforme de error (RNF-10): codigo, mensaje, detalle y timestamp. */
public record RespuestaError(String codigo, String mensaje, List<String> detalle, Instant timestamp) {}

package com.appointy.notification.domain.model;

public record Estadisticas(
        long pendientes, long enviadas, long fallidas, long canceladas,
        long confirmaciones, long reprogramaciones, long cancelaciones, long recordatorios) {}

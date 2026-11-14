package com.appointy.notification.domain.model;

/** CANCELADA: recordatorio que ya no aplica porque la cita se reprogramo o cancelo (decision 8). */
public enum EstadoNotificacion {
    PENDIENTE, ENVIADA, FALLIDA, CANCELADA
}

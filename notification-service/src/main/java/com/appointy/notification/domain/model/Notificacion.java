package com.appointy.notification.domain.model;

/**
 * Registro de un correo (RF-33). Instantes en milisegundos UTC; {@code enviadaEn} es 0 mientras no se envie.
 * {@code ultimoError} guarda solo un codigo, nunca el mensaje de la causa.
 */
public record Notificacion(
        String id,
        String citaId,
        TipoNotificacion tipo,
        String destinatarioCorreo,
        String asunto,
        String cuerpo,
        EstadoNotificacion estado,
        int intentos,
        String ultimoError,
        long programadaPara,
        long enviadaEn,
        long creadoEn) {

    public static Notificacion nueva(String id, String citaId, TipoNotificacion tipo, String correo, String asunto, String cuerpo,
            long programadaPara, long ahora) {
        return new Notificacion(id, citaId, tipo, correo, asunto, cuerpo, EstadoNotificacion.PENDIENTE, 0, null, programadaPara, 0, ahora);
    }

    public Notificacion comoEnviada(long ahora) {
        return new Notificacion(id, citaId, tipo, destinatarioCorreo, asunto, cuerpo, EstadoNotificacion.ENVIADA, intentos + 1, null,
                programadaPara, ahora, creadoEn);
    }

    public Notificacion comoFallida(String codigoError) {
        return new Notificacion(id, citaId, tipo, destinatarioCorreo, asunto, cuerpo, EstadoNotificacion.FALLIDA, intentos + 1, codigoError,
                programadaPara, enviadaEn, creadoEn);
    }

    public Notificacion comoCancelada() {
        return new Notificacion(id, citaId, tipo, destinatarioCorreo, asunto, cuerpo, EstadoNotificacion.CANCELADA, intentos, ultimoError,
                programadaPara, enviadaEn, creadoEn);
    }
}

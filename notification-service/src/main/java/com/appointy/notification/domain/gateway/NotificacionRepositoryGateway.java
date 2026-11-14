package com.appointy.notification.domain.gateway;

import com.appointy.notification.domain.model.Estadisticas;
import com.appointy.notification.domain.model.FiltroNotificaciones;
import com.appointy.notification.domain.model.Notificacion;
import com.appointy.notification.domain.model.Pagina;
import com.appointy.notification.domain.model.TipoNotificacion;

public interface NotificacionRepositoryGateway {

    Notificacion guardar(Notificacion notificacion);

    /** Devuelve null si no existe. */
    Notificacion buscarPorId(String id);

    /** Clave de idempotencia (RNF-16). Devuelve null si no existe. */
    Notificacion buscarPorClave(String citaId, TipoNotificacion tipo, long programadaPara);

    /** Recordatorios PENDIENTE con {@code programadaPara <= ahora}, los mas antiguos primero. */
    Notificacion[] recordatoriosVencidos(long ahora, int limite);

    /** Notificaciones FALLIDA con menos de {@code maxIntentos} intentos, las mas antiguas primero. */
    Notificacion[] fallidasReintentables(int maxIntentos, int limite);

    /** Marca CANCELADA los recordatorios PENDIENTE de la cita y devuelve cuantos eran. */
    int cancelarRecordatoriosPendientes(String citaId);

    Pagina listar(FiltroNotificaciones filtro, int pagina, int tamano);

    Notificacion[] listarPorCita(String citaId);

    Estadisticas estadisticas();
}

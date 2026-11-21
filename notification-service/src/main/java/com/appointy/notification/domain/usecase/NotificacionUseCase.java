package com.appointy.notification.domain.usecase;

import com.appointy.notification.domain.gateway.RelojGateway;
import com.appointy.notification.domain.gateway.GeneradorIdGateway;
import com.appointy.notification.domain.exception.NoEncontradoException;
import com.appointy.notification.domain.gateway.NotificacionRepositoryGateway;
import com.appointy.notification.domain.gateway.UsuarioClientGateway;
import com.appointy.notification.domain.model.Notificacion;
import com.appointy.notification.domain.model.ResultadoLote;
import com.appointy.notification.domain.model.SolicitudCita;
import com.appointy.notification.domain.model.TipoNotificacion;
import com.appointy.notification.domain.rule.ReglasNotificacion;

/** notificar cita, enviar recordatorios, reenviar notificacion, reintentar fallidas. */
public class NotificacionUseCase {

    private final NotificacionRepositoryGateway notificaciones;
    private final UsuarioClientGateway usuarios;
    private final Despachador despachador;
    private final RelojGateway reloj;
    private final int lote;
    private final int maxIntentos;
    private final GeneradorIdGateway ids;

    public NotificacionUseCase(NotificacionRepositoryGateway notificaciones, UsuarioClientGateway usuarios, Despachador despachador, RelojGateway reloj, int lote, int maxIntentos, GeneradorIdGateway ids) {
        this.ids = ids;
        this.notificaciones = notificaciones;
        this.usuarios = usuarios;
        this.despachador = despachador;
        this.reloj = reloj;
        this.lote = lote;
        this.maxIntentos = maxIntentos;
    }

    /** CU-33: registra y envia el correo de una cita confirmada, reprogramada o cancelada. Idempotente. */
    public Notificacion notificarCita(SolicitudCita s) {
        ReglasNotificacion.validarSolicitud(s);
        TipoNotificacion tipo = ReglasNotificacion.tipoDe(s.tipo());
        long inicio = reloj.inicioEnMillis(s.fecha(), s.horaInicio());

        // Idempotencia (RNF-16): la misma cita, tipo e inicio no genera un segundo correo.
        Notificacion existente = notificaciones.buscarPorClave(s.citaId(), tipo, inicio);
        if (existente != null) {
            return existente;
        }
        String correo = usuarios.correoDe(s.pacienteId());
        if (correo == null) {
            throw new NoEncontradoException("DESTINATARIO_NO_ENCONTRADO", "El paciente no tiene un correo registrado");
        }
        long ahora = reloj.ahora();
        if (tipo == TipoNotificacion.REPROGRAMACION || tipo == TipoNotificacion.CANCELACION) {
            notificaciones.cancelarRecordatoriosPendientes(s.citaId());
        }
        Notificacion nueva = Notificacion.nueva(ids.nuevoId(), s.citaId(), tipo, correo, ReglasNotificacion.asunto(tipo),
                ReglasNotificacion.cuerpo(tipo, s.fecha(), s.horaInicio(), s.citaId()), inicio, ahora);
        Notificacion guardada = notificaciones.guardar(nueva);
        programarRecordatorio(s, tipo, correo, inicio, ahora);
        return despachador.despachar(guardada);
    }

    private void programarRecordatorio(SolicitudCita s, TipoNotificacion tipo, String correo, long inicio, long ahora) {
        if (!ReglasNotificacion.requiereRecordatorio(tipo, inicio, ahora)) {
            return;
        }
        long momento = ReglasNotificacion.momentoRecordatorio(inicio);
        if (notificaciones.buscarPorClave(s.citaId(), TipoNotificacion.RECORDATORIO, momento) != null) {
            return;
        }
        notificaciones.guardar(Notificacion.nueva(ids.nuevoId(), s.citaId(), TipoNotificacion.RECORDATORIO, correo,
                ReglasNotificacion.asunto(TipoNotificacion.RECORDATORIO),
                ReglasNotificacion.cuerpo(TipoNotificacion.RECORDATORIO, s.fecha(), s.horaInicio(), s.citaId()), momento, ahora));
    }

    /** CU-34: envia los recordatorios que ya vencieron (24 h antes de la cita). */
    public ResultadoLote enviarRecordatorios() {
        return Lotes.procesar(notificaciones.recordatoriosVencidos(reloj.ahora(), lote), despachador);
    }

    /** Reenvio manual de una notificacion PENDIENTE o FALLIDA por un administrativo. */
    public Notificacion reenviarNotificacion(String id) {
        Notificacion notificacion = notificaciones.buscarPorId(id);
        if (notificacion == null) {
            throw new NoEncontradoException("NOTIFICACION_NO_ENCONTRADA", "La notificacion no existe");
        }
        ReglasNotificacion.validarReenvio(notificacion);
        return despachador.despachar(notificacion);
    }

    /** CU-35: reintenta las notificaciones fallidas que no llegaron al maximo de intentos. */
    public ResultadoLote reintentarFallidas() {
        return Lotes.procesar(notificaciones.fallidasReintentables(maxIntentos, lote), despachador);
    }
}

package com.appointy.notification.application.usecase;

import com.appointy.notification.domain.exception.NoEncontradoException;
import com.appointy.notification.domain.gateway.NotificacionRepositoryGateway;
import com.appointy.notification.domain.gateway.UsuarioClientGateway;
import com.appointy.notification.domain.model.Notificacion;
import com.appointy.notification.domain.model.SolicitudCita;
import com.appointy.notification.domain.model.TipoNotificacion;
import com.appointy.notification.domain.rule.ReglasNotificacion;
import com.appointy.notification.domain.usecase.NotificarCitaUseCase;
import java.time.Clock;
import java.time.ZoneId;
import java.util.UUID;

public class NotificarCitaUseCaseImpl implements NotificarCitaUseCase {

    private final NotificacionRepositoryGateway notificaciones;
    private final UsuarioClientGateway usuarios;
    private final Despachador despachador;
    private final Clock reloj;
    private final ZoneId zona;

    public NotificarCitaUseCaseImpl(NotificacionRepositoryGateway notificaciones, UsuarioClientGateway usuarios, Despachador despachador,
            Clock reloj, ZoneId zona) {
        this.notificaciones = notificaciones;
        this.usuarios = usuarios;
        this.despachador = despachador;
        this.reloj = reloj;
        this.zona = zona;
    }

    @Override
    public Notificacion ejecutar(SolicitudCita s) {
        ReglasNotificacion.validarSolicitud(s);
        TipoNotificacion tipo = ReglasNotificacion.tipoDe(s.tipo());
        long inicio = Fechas.inicioEnMillis(s.fecha(), s.horaInicio(), zona);

        // Idempotencia (RNF-16): la misma cita, tipo e inicio no genera un segundo correo.
        Notificacion existente = notificaciones.buscarPorClave(s.citaId(), tipo, inicio);
        if (existente != null) {
            return existente;
        }
        String correo = usuarios.correoDe(s.pacienteId());
        if (correo == null) {
            throw new NoEncontradoException("DESTINATARIO_NO_ENCONTRADO", "El paciente no tiene un correo registrado");
        }
        long ahora = reloj.millis();
        if (tipo == TipoNotificacion.REPROGRAMACION || tipo == TipoNotificacion.CANCELACION) {
            notificaciones.cancelarRecordatoriosPendientes(s.citaId());
        }
        Notificacion nueva = Notificacion.nueva(UUID.randomUUID().toString(), s.citaId(), tipo, correo, ReglasNotificacion.asunto(tipo),
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
        notificaciones.guardar(Notificacion.nueva(UUID.randomUUID().toString(), s.citaId(), TipoNotificacion.RECORDATORIO, correo,
                ReglasNotificacion.asunto(TipoNotificacion.RECORDATORIO),
                ReglasNotificacion.cuerpo(TipoNotificacion.RECORDATORIO, s.fecha(), s.horaInicio(), s.citaId()), momento, ahora));
    }
}

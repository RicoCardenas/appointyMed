package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.exception.ValidacionException;
import com.appointy.catalog.domain.model.Bloqueo;
import com.appointy.catalog.domain.model.Centro;
import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.domain.usecase.BloquearFranjasUseCase;
import com.appointy.catalog.domain.gateway.AppointmentClientGateway;
import com.appointy.catalog.domain.gateway.BloqueoRepositoryGateway;
import com.appointy.catalog.domain.gateway.CentroRepositoryGateway;
import com.appointy.catalog.domain.gateway.ProfesionalRepositoryGateway;
import com.appointy.catalog.domain.rule.ReglasBloqueo;
import java.time.Clock;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

/**
 * Se guarda el bloqueo y se pide a appointment que bloquee las franjas dentro de la misma transaccion: si appointment
 * rechaza (citas confirmadas) o no responde, la excepcion revierte el bloqueo y no queda nada guardado en catalog.
 * Repetir la operacion es seguro porque appointment bloquea de forma idempotente.
 */
public class BloquearFranjasUseCaseImpl implements BloquearFranjasUseCase {

    private final ProfesionalRepositoryGateway profesionales;
    private final CentroRepositoryGateway centros;
    private final BloqueoRepositoryGateway bloqueos;
    private final AppointmentClientGateway appointment;
    private final Clock reloj;

    public BloquearFranjasUseCaseImpl(
            ProfesionalRepositoryGateway profesionales,
            CentroRepositoryGateway centros,
            BloqueoRepositoryGateway bloqueos,
            AppointmentClientGateway appointment,
            Clock reloj) {
        this.profesionales = profesionales;
        this.centros = centros;
        this.bloqueos = bloqueos;
        this.appointment = appointment;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public Bloqueo ejecutar(Comando c) {
        ReglasBloqueo.validar(c.fechaInicio(), c.fechaFin(), c.motivo());
        Profesional p = profesionales.buscarPorId(c.profesionalId());
        if (p == null) {
            throw new NoEncontradoException("PROFESIONAL_NO_ENCONTRADO", "El profesional no existe");
        }
        Centro centro = centros.buscarPorId(c.centroId());
        if (centro == null) {
            throw new NoEncontradoException("CENTRO_NO_ENCONTRADO", "El centro no existe");
        }
        if (!profesionales.atiendeEnCentro(p.id(), centro.id())) {
            throw new ValidacionException("PROFESIONAL_NO_ATIENDE_EN_CENTRO", "El profesional no atiende en ese centro");
        }
        Bloqueo bloqueo = bloqueos.guardar(new Bloqueo(
                UUID.randomUUID().toString(), p.id(), centro.id(), c.fechaInicio(), c.fechaFin(), c.motivo().trim(), c.actorId(), reloj.millis()));
        appointment.bloquearFranjas(p.id(), centro.id(), c.fechaInicio(), c.fechaFin());
        return bloqueo;
    }
}

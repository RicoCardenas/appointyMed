package com.appointy.clinicalhistory.application.usecase;

import com.appointy.clinicalhistory.domain.gateway.LogAccesoRepositoryGateway;
import com.appointy.clinicalhistory.domain.model.AccionAcceso;
import com.appointy.clinicalhistory.domain.model.Actor;
import com.appointy.clinicalhistory.domain.model.LogAcceso;
import com.appointy.clinicalhistory.domain.model.ResultadoAcceso;
import java.time.Clock;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** CU-32: log de acceso de cada lectura y escritura, tambien las rechazadas (RF-30). Nunca registra contenido clinico. */
public class Auditoria {

    private static final Logger LOG = LoggerFactory.getLogger(Auditoria.class);

    private final LogAccesoRepositoryGateway logs;
    private final Clock reloj;

    public Auditoria(LogAccesoRepositoryGateway logs, Clock reloj) {
        this.logs = logs;
        this.reloj = reloj;
    }

    /** Un acceso permitido debe quedar registrado: si el registro falla, el error sube y no se entrega el dato. */
    public void permitido(Actor actor, String pacienteId, AccionAcceso accion, String entradaId) {
        logs.registrar(nuevo(actor, pacienteId, accion, entradaId, ResultadoAcceso.PERMITIDO));
    }

    /** Se llama al rechazar; si el propio registro falla se anota (sin datos) y se conserva el error original. */
    public void denegado(Actor actor, String pacienteId, AccionAcceso accion, String entradaId) {
        try {
            logs.registrar(nuevo(actor, pacienteId, accion, entradaId, ResultadoAcceso.DENEGADO));
        } catch (RuntimeException e) {
            LOG.error("No se pudo registrar el acceso denegado: {}", e.getClass().getSimpleName());
        }
    }

    public LogAcceso nuevo(Actor actor, String pacienteId, AccionAcceso accion, String entradaId, ResultadoAcceso resultado) {
        return new LogAcceso(UUID.randomUUID().toString(), actor.usuarioId(), actor.rol(), pacienteId, accion, entradaId,
                reloj.millis(), resultado);
    }
}

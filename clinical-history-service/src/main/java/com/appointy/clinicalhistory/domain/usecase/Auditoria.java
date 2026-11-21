package com.appointy.clinicalhistory.domain.usecase;

import com.appointy.clinicalhistory.domain.gateway.RelojGateway;
import com.appointy.clinicalhistory.domain.gateway.GeneradorIdGateway;
import com.appointy.clinicalhistory.domain.gateway.LogAccesoRepositoryGateway;
import com.appointy.clinicalhistory.domain.gateway.RegistroGateway;
import com.appointy.clinicalhistory.domain.model.AccionAcceso;
import com.appointy.clinicalhistory.domain.model.Actor;
import com.appointy.clinicalhistory.domain.model.LogAcceso;
import com.appointy.clinicalhistory.domain.model.ResultadoAcceso;

/** CU-32: log de acceso de cada lectura y escritura, tambien las rechazadas (RF-30). Nunca registra contenido clinico. */
public class Auditoria {

    private final LogAccesoRepositoryGateway logs;
    private final RelojGateway reloj;
    private final RegistroGateway registro;
    private final GeneradorIdGateway ids;

    public Auditoria(LogAccesoRepositoryGateway logs, RelojGateway reloj, RegistroGateway registro, GeneradorIdGateway ids) {
        this.ids = ids;
        this.logs = logs;
        this.reloj = reloj;
        this.registro = registro;
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
            registro.error("No se pudo registrar el acceso denegado: " + e.getClass().getSimpleName());
        }
    }

    public LogAcceso nuevo(Actor actor, String pacienteId, AccionAcceso accion, String entradaId, ResultadoAcceso resultado) {
        return new LogAcceso(ids.nuevoId(), actor.usuarioId(), actor.rol(), pacienteId, accion, entradaId,
                reloj.ahora(), resultado);
    }
}

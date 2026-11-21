package com.appointy.catalog.domain.usecase;

import com.appointy.catalog.domain.gateway.RelojGateway;
import com.appointy.catalog.domain.gateway.GeneradorIdGateway;
import com.appointy.catalog.domain.gateway.TransactionGateway;
import com.appointy.catalog.domain.exception.ConflictoException;
import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.exception.ServicioNoDisponibleException;
import com.appointy.catalog.domain.exception.ValidacionException;
import com.appointy.catalog.domain.gateway.AppointmentClientGateway;
import com.appointy.catalog.domain.gateway.BloqueoRepositoryGateway;
import com.appointy.catalog.domain.gateway.CentroRepositoryGateway;
import com.appointy.catalog.domain.gateway.HorarioRepositoryGateway;
import com.appointy.catalog.domain.gateway.ParametroRepositoryGateway;
import com.appointy.catalog.domain.gateway.ProfesionalRepositoryGateway;
import com.appointy.catalog.domain.model.Bloqueo;
import com.appointy.catalog.domain.model.Centro;
import com.appointy.catalog.domain.model.HorarioAtencion;
import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.domain.model.ResultadoHorario;
import com.appointy.catalog.domain.rule.ReglasBloqueo;
import com.appointy.catalog.domain.rule.ReglasHorario;
import com.appointy.catalog.domain.rule.ReglasParametro;

/** definir horario, modificar horario, listar horarios, generar franjas, bloquear franjas. */
public class HorarioUseCase {

    public record DefinirHorarioComando(String profesionalId, String centroId, int diaSemana, int horaInicio, int horaFin, Integer duracionFranjaMin) {}

    public record ModificarHorarioComando(String horarioId, String centroId, int diaSemana, int horaInicio, int horaFin, Integer duracionFranjaMin, boolean activo) {}

    public record BloquearFranjasComando(String actorId, String profesionalId, String centroId, String fechaInicio, String fechaFin, String motivo) {}

    private final ProfesionalRepositoryGateway profesionales;
    private final CentroRepositoryGateway centros;
    private final HorarioRepositoryGateway horarios;
    private final ParametroRepositoryGateway parametros;
    private final PublicadorDeFranjas publicador;
    private final BloqueoRepositoryGateway bloqueos;
    private final AppointmentClientGateway appointment;
    private final RelojGateway reloj;
    private final TransactionGateway transacciones;
    private final GeneradorIdGateway ids;

    public HorarioUseCase(ProfesionalRepositoryGateway profesionales, CentroRepositoryGateway centros, HorarioRepositoryGateway horarios, ParametroRepositoryGateway parametros, PublicadorDeFranjas publicador, BloqueoRepositoryGateway bloqueos, AppointmentClientGateway appointment, RelojGateway reloj, TransactionGateway transacciones, GeneradorIdGateway ids) {
        this.ids = ids;
        this.transacciones = transacciones;
        this.profesionales = profesionales;
        this.centros = centros;
        this.horarios = horarios;
        this.parametros = parametros;
        this.publicador = publicador;
        this.bloqueos = bloqueos;
        this.appointment = appointment;
        this.reloj = reloj;
    }

    /** CU-13: define un horario y pide a appointment generar las franjas. Sin duracion se usa el parametro duracion_franja_min. */
    public ResultadoHorario definirHorario(DefinirHorarioComando c) {
        Profesional p = profesionales.buscarPorId(c.profesionalId());
        if (p == null) {
            throw new NoEncontradoException("PROFESIONAL_NO_ENCONTRADO", "El profesional no existe");
        }
        if (!p.activo()) {
            throw new ValidacionException("PROFESIONAL_INACTIVO", "El profesional esta inactivo");
        }
        Centro centro = centros.buscarPorId(c.centroId());
        if (centro == null) {
            throw new NoEncontradoException("CENTRO_NO_ENCONTRADO", "El centro no existe");
        }
        if (!centro.activo()) {
            throw new ValidacionException("CENTRO_INACTIVO", "El centro esta inactivo");
        }
        if (!profesionales.atiendeEnCentro(p.id(), centro.id())) {
            throw new ValidacionException("PROFESIONAL_NO_ATIENDE_EN_CENTRO", "El profesional no atiende en ese centro");
        }
        int duracion = c.duracionFranjaMin() != null ? c.duracionFranjaMin() : Parametros.entero(parametros, ReglasParametro.DURACION_FRANJA_MIN);
        ReglasHorario.validar(c.diaSemana(), c.horaInicio(), c.horaFin(), duracion);
        HorarioAtencion nuevo = new HorarioAtencion(ids.nuevoId(), p.id(), centro.id(), c.diaSemana(), c.horaInicio(), c.horaFin(), duracion, true);
        ReglasHorario.validarSinTraslape(nuevo, horarios.listarDeProfesional(p.id()));
        return publicador.publicar(horarios.guardar(nuevo));
    }

    /** CU-14: modifica un horario y pide generar las franjas nuevas. */
    public ResultadoHorario modificarHorario(ModificarHorarioComando c) {
        HorarioAtencion actual = horarios.buscarPorId(c.horarioId());
        if (actual == null) {
            throw new NoEncontradoException("HORARIO_NO_ENCONTRADO", "El horario no existe");
        }
        Centro centro = centros.buscarPorId(c.centroId());
        if (centro == null) {
            throw new NoEncontradoException("CENTRO_NO_ENCONTRADO", "El centro no existe");
        }
        if (!centro.activo()) {
            throw new ValidacionException("CENTRO_INACTIVO", "El centro esta inactivo");
        }
        if (!profesionales.atiendeEnCentro(actual.profesionalId(), centro.id())) {
            throw new ValidacionException("PROFESIONAL_NO_ATIENDE_EN_CENTRO", "El profesional no atiende en ese centro");
        }
        int duracion = c.duracionFranjaMin() != null ? c.duracionFranjaMin() : Parametros.entero(parametros, ReglasParametro.DURACION_FRANJA_MIN);
        ReglasHorario.validar(c.diaSemana(), c.horaInicio(), c.horaFin(), duracion);
        HorarioAtencion modificado = actual.modificar(centro.id(), c.diaSemana(), c.horaInicio(), c.horaFin(), duracion, c.activo());
        if (modificado.activo()) {
            ReglasHorario.validarSinTraslape(modificado, horarios.listarDeProfesional(actual.profesionalId()));
        }
        return publicador.publicar(horarios.guardar(modificado));
    }

    /** Horarios de un profesional. */
    public HorarioAtencion[] listarHorarios(String profesionalId) {
        return transacciones.ejecutarSoloLectura(() -> {
            if (profesionales.buscarPorId(profesionalId) == null) {
                throw new NoEncontradoException("PROFESIONAL_NO_ENCONTRADO", "El profesional no existe");
            }
            return horarios.listarDeProfesional(profesionalId);
        });
    }

    /** CU-13/CU-25: reintenta la generacion de franjas de un horario (idempotente). */
    public ResultadoHorario generarFranjas(String horarioId) {
        HorarioAtencion horario = horarios.buscarPorId(horarioId);
        if (horario == null) {
            throw new NoEncontradoException("HORARIO_NO_ENCONTRADO", "El horario no existe");
        }
        if (!horario.activo()) {
            throw new ConflictoException("HORARIO_INACTIVO", "El horario esta inactivo: no se generan franjas");
        }
        ResultadoHorario resultado = publicador.publicar(horario);
        if (!resultado.franjasGeneradas()) {
            throw new ServicioNoDisponibleException("APPOINTMENT_NO_DISPONIBLE", "El servicio de citas no responde; intenta de nuevo mas tarde");
        }
        return resultado;
    }

    /** CU-16: registra un bloqueo si appointment confirma que no hay citas CONFIRMADAS en el intervalo. */
    public Bloqueo bloquearFranjas(BloquearFranjasComando c) {
        return transacciones.ejecutar(() -> {
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
                    ids.nuevoId(), p.id(), centro.id(), c.fechaInicio(), c.fechaFin(), c.motivo().trim(), c.actorId(), reloj.ahora()));
            appointment.bloquearFranjas(p.id(), centro.id(), c.fechaInicio(), c.fechaFin());
            return bloqueo;
        });
    }
}

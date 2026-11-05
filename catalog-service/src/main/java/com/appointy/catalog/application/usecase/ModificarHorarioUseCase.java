package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.exception.ValidacionException;
import com.appointy.catalog.domain.model.Centro;
import com.appointy.catalog.domain.model.HorarioAtencion;
import com.appointy.catalog.domain.model.ResultadoHorario;
import com.appointy.catalog.domain.port.in.ModificarHorarioPort;
import com.appointy.catalog.domain.port.out.CentroRepositoryPort;
import com.appointy.catalog.domain.port.out.HorarioRepositoryPort;
import com.appointy.catalog.domain.port.out.ParametroRepositoryPort;
import com.appointy.catalog.domain.port.out.ProfesionalRepositoryPort;
import com.appointy.catalog.domain.rule.ReglasHorario;
import com.appointy.catalog.domain.rule.ReglasParametro;

/**
 * Las franjas ya generadas de la version anterior del horario no se retiran: para dejar de ofrecerlas se usa un bloqueo (CU-16).
 */
public class ModificarHorarioUseCase implements ModificarHorarioPort {

    private final ProfesionalRepositoryPort profesionales;
    private final CentroRepositoryPort centros;
    private final HorarioRepositoryPort horarios;
    private final ParametroRepositoryPort parametros;
    private final PublicadorDeFranjas publicador;

    public ModificarHorarioUseCase(
            ProfesionalRepositoryPort profesionales,
            CentroRepositoryPort centros,
            HorarioRepositoryPort horarios,
            ParametroRepositoryPort parametros,
            PublicadorDeFranjas publicador) {
        this.profesionales = profesionales;
        this.centros = centros;
        this.horarios = horarios;
        this.parametros = parametros;
        this.publicador = publicador;
    }

    @Override
    public ResultadoHorario ejecutar(Comando c) {
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
}

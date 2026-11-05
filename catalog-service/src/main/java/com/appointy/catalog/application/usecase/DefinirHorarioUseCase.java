package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.exception.ValidacionException;
import com.appointy.catalog.domain.model.Centro;
import com.appointy.catalog.domain.model.HorarioAtencion;
import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.domain.model.ResultadoHorario;
import com.appointy.catalog.domain.port.in.DefinirHorarioPort;
import com.appointy.catalog.domain.port.out.CentroRepositoryPort;
import com.appointy.catalog.domain.port.out.HorarioRepositoryPort;
import com.appointy.catalog.domain.port.out.ParametroRepositoryPort;
import com.appointy.catalog.domain.port.out.ProfesionalRepositoryPort;
import com.appointy.catalog.domain.rule.ReglasHorario;
import com.appointy.catalog.domain.rule.ReglasParametro;
import java.util.UUID;

/** Sin transaccion externa: el horario se guarda y despues se llama a appointment, para no retener la conexion durante la llamada. */
public class DefinirHorarioUseCase implements DefinirHorarioPort {

    private final ProfesionalRepositoryPort profesionales;
    private final CentroRepositoryPort centros;
    private final HorarioRepositoryPort horarios;
    private final ParametroRepositoryPort parametros;
    private final PublicadorDeFranjas publicador;

    public DefinirHorarioUseCase(
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
        HorarioAtencion nuevo = new HorarioAtencion(UUID.randomUUID().toString(), p.id(), centro.id(), c.diaSemana(), c.horaInicio(), c.horaFin(), duracion, true);
        ReglasHorario.validarSinTraslape(nuevo, horarios.listarDeProfesional(p.id()));
        return publicador.publicar(horarios.guardar(nuevo));
    }
}

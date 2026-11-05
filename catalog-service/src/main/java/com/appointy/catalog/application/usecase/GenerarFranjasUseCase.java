package com.appointy.catalog.application.usecase;

import com.appointy.catalog.domain.exception.ConflictoException;
import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.exception.ServicioNoDisponibleException;
import com.appointy.catalog.domain.model.HorarioAtencion;
import com.appointy.catalog.domain.model.ResultadoHorario;
import com.appointy.catalog.domain.port.in.GenerarFranjasPort;
import com.appointy.catalog.domain.port.out.HorarioRepositoryPort;

/** Reintento explicito: a diferencia de definir o modificar, aqui un fallo de appointment se informa como error (503). */
public class GenerarFranjasUseCase implements GenerarFranjasPort {

    private final HorarioRepositoryPort horarios;
    private final PublicadorDeFranjas publicador;

    public GenerarFranjasUseCase(HorarioRepositoryPort horarios, PublicadorDeFranjas publicador) {
        this.horarios = horarios;
        this.publicador = publicador;
    }

    @Override
    public ResultadoHorario ejecutar(String horarioId) {
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
}

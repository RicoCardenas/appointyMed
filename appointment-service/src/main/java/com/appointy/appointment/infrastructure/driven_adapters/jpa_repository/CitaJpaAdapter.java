package com.appointy.appointment.infrastructure.driven_adapters.jpa_repository;

import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.EstadoCita;
import com.appointy.appointment.domain.gateway.CitaRepositoryGateway;
import com.appointy.appointment.infrastructure.driven_adapters.jpa_repository.repository.SpringDataCitaRepository;
import com.appointy.appointment.infrastructure.mapper.CitaEntityMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CitaJpaAdapter implements CitaRepositoryGateway {

    private final SpringDataCitaRepository repositorio;
    private final CitaEntityMapper mapper;
    private final BloqueosPostgres bloqueos;

    CitaJpaAdapter(SpringDataCitaRepository repositorio, CitaEntityMapper mapper, BloqueosPostgres bloqueos) {
        this.repositorio = repositorio;
        this.mapper = mapper;
        this.bloqueos = bloqueos;
    }

    /** Escribe de inmediato: si el indice uq_cita_franja_confirmada rechaza el duplicado, el error sale dentro del caso de uso. */
    @Override
    public Cita guardar(Cita cita) {
        return mapper.aDominio(repositorio.saveAndFlush(mapper.aEntidad(cita)));
    }

    @Override
    public Cita buscarPorId(String id) {
        return repositorio.findById(UUID.fromString(id)).map(mapper::aDominio).orElse(null);
    }

    @Override
    public void bloquearPaciente(String pacienteId) {
        bloqueos.bloquearClave("paciente:" + pacienteId);
    }

    @Override
    public Cita[] listarDePaciente(String pacienteId, String desde, String hasta) {
        return repositorio.listarDePaciente(UUID.fromString(pacienteId), LocalDate.parse(desde), LocalDate.parse(hasta))
                .stream().map(mapper::aDominio).toArray(Cita[]::new);
    }

    @Override
    public Cita[] listarAgendaConfirmada(String profesionalId, String desde, String hasta) {
        return repositorio.listarDeProfesional(UUID.fromString(profesionalId), EstadoCita.CONFIRMADA, LocalDate.parse(desde), LocalDate.parse(hasta))
                .stream().map(mapper::aDominio).toArray(Cita[]::new);
    }

    @Override
    public String[] idsConfirmadasEnRango(String profesionalId, String centroId, String fechaInicio, String fechaFin) {
        return repositorio.idsEnRango(UUID.fromString(profesionalId), UUID.fromString(centroId), EstadoCita.CONFIRMADA,
                LocalDate.parse(fechaInicio), LocalDate.parse(fechaFin)).stream().map(UUID::toString).toArray(String[]::new);
    }

    @Override
    public boolean existeRelacion(String profesionalId, String pacienteId) {
        List<EstadoCita> estados = List.of(EstadoCita.CONFIRMADA, EstadoCita.COMPLETADA, EstadoCita.NO_ASISTIDA);
        return repositorio.existsByProfesionalIdAndPacienteIdAndEstadoIn(UUID.fromString(profesionalId), UUID.fromString(pacienteId), estados);
    }
}

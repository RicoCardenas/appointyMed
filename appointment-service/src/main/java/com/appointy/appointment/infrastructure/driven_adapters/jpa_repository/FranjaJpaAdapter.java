package com.appointy.appointment.infrastructure.driven_adapters.jpa_repository;

import com.appointy.appointment.domain.model.EstadoCita;
import com.appointy.appointment.domain.model.EstadoFranja;
import com.appointy.appointment.domain.model.Franja;
import com.appointy.appointment.domain.gateway.FranjaRepositoryGateway;
import com.appointy.appointment.infrastructure.driven_adapters.jpa_repository.repository.SpringDataFranjaRepository;
import com.appointy.appointment.infrastructure.mapper.Conversiones;
import com.appointy.appointment.infrastructure.mapper.FranjaEntityMapper;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class FranjaJpaAdapter implements FranjaRepositoryGateway {

    private final SpringDataFranjaRepository repositorio;
    private final FranjaEntityMapper mapper;
    private final Conversiones conversiones;
    private final BloqueosPostgres bloqueos;

    FranjaJpaAdapter(SpringDataFranjaRepository repositorio, FranjaEntityMapper mapper, Conversiones conversiones, BloqueosPostgres bloqueos) {
        this.repositorio = repositorio;
        this.mapper = mapper;
        this.conversiones = conversiones;
        this.bloqueos = bloqueos;
    }

    @Override
    public Franja buscarPorId(String id) {
        return repositorio.findById(UUID.fromString(id)).map(mapper::aDominio).orElse(null);
    }

    @Override
    public Franja buscarParaActualizar(String id) {
        bloqueos.limitarEspera();
        return repositorio.buscarParaActualizar(UUID.fromString(id)).map(mapper::aDominio).orElse(null);
    }

    @Override
    public Franja[] bloquearRango(String profesionalId, String centroId, String fechaInicio, String fechaFin) {
        bloqueos.limitarEspera();
        return repositorio.bloquearRango(UUID.fromString(profesionalId), UUID.fromString(centroId), LocalDate.parse(fechaInicio),
                LocalDate.parse(fechaFin)).stream().map(mapper::aDominio).toArray(Franja[]::new);
    }

    @Override
    public Franja[] buscarPorIds(String[] ids) {
        if (ids.length == 0) {
            return new Franja[0];
        }
        List<UUID> uuids = Arrays.stream(ids).map(UUID::fromString).toList();
        return repositorio.findByIdIn(uuids).stream().map(mapper::aDominio).toArray(Franja[]::new);
    }

    @Override
    public Franja[] listarDisponibles(String profesionalId, String centroId, String fecha) {
        return repositorio.findByProfesionalIdAndCentroIdAndFechaAndEstadoOrderByHoraInicio(
                UUID.fromString(profesionalId), UUID.fromString(centroId), LocalDate.parse(fecha), EstadoFranja.DISPONIBLE)
                .stream().map(mapper::aDominio).toArray(Franja[]::new);
    }

    @Override
    public Franja[] franjasDeCitasConfirmadasDelPaciente(String pacienteId, String fecha) {
        return repositorio.franjasDeCitasDelPaciente(UUID.fromString(pacienteId), LocalDate.parse(fecha), EstadoCita.CONFIRMADA)
                .stream().map(mapper::aDominio).toArray(Franja[]::new);
    }

    /** Escribe de inmediato para que un conflicto de version salga dentro del caso de uso y no al confirmar. */
    @Override
    public Franja guardar(Franja franja) {
        return mapper.aDominio(repositorio.saveAndFlush(mapper.aEntidad(franja)));
    }

    @Override
    public boolean insertarSiNoExiste(Franja f) {
        return repositorio.insertarSiNoExiste(conversiones.textoAId(f.id()), conversiones.textoAId(f.profesionalId()),
                conversiones.textoAId(f.centroId()), conversiones.textoAFecha(f.fecha()), conversiones.minutosAHora(f.horaInicio()),
                conversiones.minutosAHora(f.horaFin())) > 0;
    }
}

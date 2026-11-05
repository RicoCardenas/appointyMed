package com.appointy.appointment.infrastructure.driven_adapters.jpa_repository;

import com.appointy.appointment.domain.model.HistorialCita;
import com.appointy.appointment.domain.gateway.HistorialCitaRepositoryGateway;
import com.appointy.appointment.infrastructure.driven_adapters.jpa_repository.repository.SpringDataHistorialCitaRepository;
import com.appointy.appointment.infrastructure.mapper.HistorialCitaEntityMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class HistorialCitaJpaAdapter implements HistorialCitaRepositoryGateway {

    @PersistenceContext
    private EntityManager em;

    private final SpringDataHistorialCitaRepository repositorio;
    private final HistorialCitaEntityMapper mapper;

    HistorialCitaJpaAdapter(SpringDataHistorialCitaRepository repositorio, HistorialCitaEntityMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    /** Solo inserta: {@code persist} nunca actualiza una fila existente. */
    @Override
    public void registrar(HistorialCita fila) {
        em.persist(mapper.aEntidad(fila));
    }

    @Override
    public HistorialCita[] listarPorCita(String citaId) {
        return repositorio.findByCitaIdOrderByFechaAsc(UUID.fromString(citaId)).stream().map(mapper::aDominio).toArray(HistorialCita[]::new);
    }
}

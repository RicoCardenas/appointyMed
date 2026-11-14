package com.appointy.clinicalhistory.infrastructure.driven_adapters.jpa_repository;

import com.appointy.clinicalhistory.domain.gateway.EntradaClinicaRepositoryGateway;
import com.appointy.clinicalhistory.domain.model.EntradaClinica;
import com.appointy.clinicalhistory.infrastructure.driven_adapters.jpa_repository.repository.SpringDataEntradaClinicaRepository;
import com.appointy.clinicalhistory.infrastructure.mapper.ClinicaEntityMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.appointy.clinicalhistory.domain.model.PaginaEntradas;
import com.appointy.clinicalhistory.infrastructure.driven_adapters.jpa_repository.entity.EntradaClinicaEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class EntradaClinicaJpaAdapter implements EntradaClinicaRepositoryGateway {

    @PersistenceContext
    private EntityManager em;

    private final SpringDataEntradaClinicaRepository repositorio;
    private final ClinicaEntityMapper mapper;

    EntradaClinicaJpaAdapter(SpringDataEntradaClinicaRepository repositorio, ClinicaEntityMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    /** Solo inserta: {@code persist} nunca actualiza una fila existente. Escribe de inmediato para que el indice unico falle aqui. */
    @Override
    @Transactional
    public void registrar(EntradaClinica entrada) {
        em.persist(mapper.aEntidad(entrada));
        em.flush();
    }

    @Override
    @Transactional(readOnly = true)
    public EntradaClinica buscarPorId(String id) {
        return repositorio.findById(UUID.fromString(id)).map(mapper::aDominio).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public EntradaClinica[] listarDePaciente(String pacienteId) {
        return repositorio.findByPacienteIdOrderByCreadoEnAscIdAsc(UUID.fromString(pacienteId)).stream().map(mapper::aDominio).toArray(EntradaClinica[]::new);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeOriginalDeCita(String citaId) {
        return repositorio.existsByCitaIdAndCorrigeAIdIsNull(UUID.fromString(citaId));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaEntradas listarDeProfesional(String profesionalId, int pagina, int tamano) {
        Page<EntradaClinicaEntity> resultado = repositorio.findByProfesionalId(UUID.fromString(profesionalId),
                PageRequest.of(pagina, tamano, Sort.by(Sort.Order.desc("creadoEn"), Sort.Order.asc("id"))));
        return new PaginaEntradas(resultado.getContent().stream().map(mapper::aDominio).toArray(EntradaClinica[]::new), pagina, tamano,
                resultado.getTotalElements(), resultado.getTotalPages());
    }
}

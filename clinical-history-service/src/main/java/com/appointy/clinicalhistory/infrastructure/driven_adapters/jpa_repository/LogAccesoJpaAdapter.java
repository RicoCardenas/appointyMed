package com.appointy.clinicalhistory.infrastructure.driven_adapters.jpa_repository;

import com.appointy.clinicalhistory.domain.gateway.LogAccesoRepositoryGateway;
import com.appointy.clinicalhistory.domain.model.LogAcceso;
import com.appointy.clinicalhistory.infrastructure.mapper.ClinicaEntityMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class LogAccesoJpaAdapter implements LogAccesoRepositoryGateway {

    @PersistenceContext
    private EntityManager em;

    private final ClinicaEntityMapper mapper;

    LogAccesoJpaAdapter(ClinicaEntityMapper mapper) {
        this.mapper = mapper;
    }

    /** Se une a la transaccion de la escritura clinica; fuera de ella abre la suya y se confirma al terminar. */
    @Override
    @Transactional
    public void registrar(LogAcceso log) {
        em.persist(mapper.aEntidad(log));
    }
}

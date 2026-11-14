package com.appointy.catalog.infrastructure.driven_adapters.jpa_repository;

import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.domain.gateway.ProfesionalRepositoryGateway;
import com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.entity.ProfesionalEntity;
import com.appointy.catalog.infrastructure.driven_adapters.jpa_repository.repository.SpringDataProfesionalRepository;
import com.appointy.catalog.infrastructure.mapper.ProfesionalEntityMapper;
import com.appointy.catalog.domain.model.FiltroProfesionales;
import com.appointy.catalog.domain.model.PaginaProfesionales;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProfesionalJpaAdapter implements ProfesionalRepositoryGateway {

    private final SpringDataProfesionalRepository repositorio;
    private final ProfesionalEntityMapper mapper;

    public ProfesionalJpaAdapter(SpringDataProfesionalRepository repositorio, ProfesionalEntityMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public Profesional guardar(Profesional profesional) {
        return mapper.aDominio(repositorio.save(mapper.aEntidad(profesional)));
    }

    @Override
    public Profesional buscarPorId(String id) {
        return repositorio.findById(UUID.fromString(id)).map(mapper::aDominio).orElse(null);
    }

    @Override
    public Profesional buscarPorUsuarioId(String usuarioId) {
        ProfesionalEntity e = repositorio.findByUsuarioId(UUID.fromString(usuarioId));
        return e == null ? null : mapper.aDominio(e);
    }

    @Override
    public Profesional buscarPorRegistroMedico(String registroMedico) {
        ProfesionalEntity e = repositorio.findByRegistroMedico(registroMedico);
        return e == null ? null : mapper.aDominio(e);
    }

    @Override
    public PaginaProfesionales listar(FiltroProfesionales filtro, int pagina, int tamano) {
        Specification<ProfesionalEntity> spec = (raiz, consulta, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (filtro != null && filtro.activo() != null) condiciones.add(cb.equal(raiz.get("activo"), filtro.activo()));
            if (filtro != null && filtro.texto() != null && !filtro.texto().isBlank()) {
                String patron = "%" + filtro.texto().trim().toLowerCase().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
                condiciones.add(cb.or(cb.like(cb.lower(raiz.get("nombres")), patron, '\\'), cb.like(cb.lower(raiz.get("apellidos")), patron, '\\'),
                        cb.like(cb.lower(raiz.get("registroMedico")), patron, '\\')));
            }
            return cb.and(condiciones.toArray(new Predicate[0]));
        };
        Page<ProfesionalEntity> resultado = repositorio.findAll(spec,
                PageRequest.of(pagina, tamano, Sort.by(Sort.Order.asc("apellidos"), Sort.Order.asc("nombres"), Sort.Order.asc("id"))));
        return new PaginaProfesionales(resultado.getContent().stream().map(mapper::aDominio).toArray(Profesional[]::new), pagina, tamano,
                resultado.getTotalElements(), resultado.getTotalPages());
    }

    @Override
    public Profesional[] listarActivosPorEspecialidad(String especialidadId) {
        return repositorio.listarActivosPorEspecialidad(UUID.fromString(especialidadId)).stream().map(mapper::aDominio).toArray(Profesional[]::new);
    }

    @Override
    public String[] idsEspecialidades(String profesionalId) {
        return repositorio.idsEspecialidades(UUID.fromString(profesionalId)).stream().map(UUID::toString).sorted().toArray(String[]::new);
    }

    @Override
    public String[] idsCentros(String profesionalId) {
        return repositorio.idsCentros(UUID.fromString(profesionalId)).stream().map(UUID::toString).sorted().toArray(String[]::new);
    }

    @Override
    @Transactional
    public void reemplazarEspecialidades(String profesionalId, String[] especialidadIds) {
        UUID id = UUID.fromString(profesionalId);
        repositorio.borrarEspecialidades(id);
        for (String especialidadId : especialidadIds) {
            repositorio.insertarEspecialidad(id, UUID.fromString(especialidadId));
        }
    }

    @Override
    @Transactional
    public void reemplazarCentros(String profesionalId, String[] centroIds) {
        UUID id = UUID.fromString(profesionalId);
        repositorio.borrarCentros(id);
        for (String centroId : centroIds) {
            repositorio.insertarCentro(id, UUID.fromString(centroId));
        }
    }

    @Override
    public boolean atiendeEnCentro(String profesionalId, String centroId) {
        return repositorio.atiendeEnCentro(UUID.fromString(profesionalId), UUID.fromString(centroId));
    }

    @Override
    public boolean tieneEspecialidad(String profesionalId, String especialidadId) {
        return repositorio.tieneEspecialidad(UUID.fromString(profesionalId), UUID.fromString(especialidadId));
    }
}

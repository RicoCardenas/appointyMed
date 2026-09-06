package com.appointymed.auth.infrastructure.persistence.repository;

import com.appointymed.auth.domain.model.Role;
import com.appointymed.auth.domain.model.DomainList;
import com.appointymed.auth.domain.model.DomainOptional;
import com.appointymed.auth.domain.repository.RoleRepository;
import com.appointymed.auth.infrastructure.persistence.entity.RoleJpaEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class RoleRepositoryImpl implements RoleRepository {
    private final SpringDataRoleRepository springDataRoleRepository;

    public RoleRepositoryImpl(SpringDataRoleRepository springDataRoleRepository) {
        this.springDataRoleRepository = springDataRoleRepository;
    }

    @Override
    public DomainOptional<Role> findByName(String name) {
        Optional<RoleJpaEntity> entity = springDataRoleRepository.findByName(name);
        return entity.isPresent() ? DomainOptional.of(Role.fromName(entity.get().getName())) : DomainOptional.empty();
    }

    @Override
    public DomainList<Role> findAll() {
        DomainList<Role> result = new DomainList<>();
        for (RoleJpaEntity entity : springDataRoleRepository.findAll()) result.add(Role.fromName(entity.getName()));
        return result;
    }

    @Override
    public DomainList<Role> findByNames(DomainList<String> names) {
        List<String> values = new java.util.ArrayList<>();
        for (int index = 0; index < names.size(); index++) values.add(names.get(index));
        DomainList<Role> result = new DomainList<>();
        for (RoleJpaEntity entity : springDataRoleRepository.findByNameIn(values)) result.add(Role.fromName(entity.getName()));
        return result;
    }
}

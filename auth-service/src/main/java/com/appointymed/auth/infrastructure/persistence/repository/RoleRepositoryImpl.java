package com.appointymed.auth.infrastructure.persistence.repository;

import com.appointymed.auth.domain.model.Role;
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
    public Optional<Role> findByName(String name) {
        return springDataRoleRepository.findByName(name)
                .map(roleEntity -> Role.fromName(roleEntity.getName()));
    }

    @Override
    public List<Role> findAll() {
        return springDataRoleRepository.findAll().stream()
                .map(roleEntity -> Role.fromName(roleEntity.getName()))
                .toList();
    }

    @Override
    public List<Role> findByNames(List<String> names) {
        return springDataRoleRepository.findByNameIn(names).stream()
                .map(roleEntity -> Role.fromName(roleEntity.getName()))
                .toList();
    }
}
package com.appointymed.auth.infrastructure.persistence.repository;

import com.appointymed.auth.domain.model.Email;
import com.appointymed.auth.domain.model.Role;
import com.appointymed.auth.domain.model.User;
import com.appointymed.auth.domain.model.UserStatus;
import com.appointymed.auth.domain.repository.UserRepository;
import com.appointymed.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.appointymed.auth.infrastructure.persistence.entity.RoleJpaEntity;
import com.appointymed.auth.infrastructure.persistence.mapper.UserPersistenceMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final SpringDataUserRepository springDataUserRepository;
    private final SpringDataRoleRepository springDataRoleRepository;

    public UserRepositoryImpl(SpringDataUserRepository springDataUserRepository,
                              SpringDataRoleRepository springDataRoleRepository) {
        this.springDataUserRepository = springDataUserRepository;
        this.springDataRoleRepository = springDataRoleRepository;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return springDataUserRepository.findById(id)
                .map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return springDataUserRepository.findByEmail(email.getValue())
                .map(UserPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return springDataUserRepository.existsByEmail(email.getValue());
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = UserPersistenceMapper.toEntity(user);
        entity.setRoles(resolveRoles(user.getRoles()));
        UserJpaEntity saved = springDataUserRepository.save(entity);
        return UserPersistenceMapper.toDomain(saved);
    }

    private Set<RoleJpaEntity> resolveRoles(Set<Role> roles) {
        Set<String> roleNames = roles.stream().map(Enum::name).collect(Collectors.toSet());
        Set<RoleJpaEntity> entities = springDataRoleRepository.findByNameIn(roleNames.stream().toList()).stream().collect(Collectors.toSet());
        if (entities.size() != roleNames.size()) {
            throw new IllegalStateException("One or more requested roles do not exist in the database");
        }
        return entities;
    }

    @Override
    public List<User> findAll(int page, int size) {
        return springDataUserRepository.findAll(PageRequest.of(page, size))
                .stream()
                .map(UserPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<User> findByStatus(UserStatus status, int page, int size) {
        return springDataUserRepository.findByStatus(
                        UserJpaEntity.UserStatus.valueOf(status.name()),
                        PageRequest.of(page, size))
                .stream()
                .map(UserPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public long count() {
        return springDataUserRepository.count();
    }

    @Override
    public long countByStatus(UserStatus status) {
        return springDataUserRepository.findByStatus(
                UserJpaEntity.UserStatus.valueOf(status.name())).size();
    }

    @Override
    public void deleteById(UUID id) {
        springDataUserRepository.deleteById(id);
    }
}
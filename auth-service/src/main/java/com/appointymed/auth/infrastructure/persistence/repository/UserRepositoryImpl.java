package com.appointymed.auth.infrastructure.persistence.repository;

import com.appointymed.auth.domain.model.Email;
import com.appointymed.auth.domain.model.DomainId;
import com.appointymed.auth.domain.model.DomainList;
import com.appointymed.auth.domain.model.DomainOptional;
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
    public DomainOptional<User> findById(DomainId id) {
        Optional<UserJpaEntity> entity = springDataUserRepository.findById(UUID.fromString(id.value()));
        return entity.isPresent() ? DomainOptional.of(UserPersistenceMapper.toDomain(entity.get())) : DomainOptional.empty();
    }

    @Override
    public DomainOptional<User> findByEmail(Email email) {
        Optional<UserJpaEntity> entity = springDataUserRepository.findByEmail(email.getValue());
        return entity.isPresent() ? DomainOptional.of(UserPersistenceMapper.toDomain(entity.get())) : DomainOptional.empty();
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

    private Set<RoleJpaEntity> resolveRoles(com.appointymed.auth.domain.model.DomainSet<Role> roles) {
        Set<String> roleNames = new java.util.HashSet<>();
        for (int index = 0; index < roles.size(); index++) roleNames.add(roles.get(index).name());
        Set<RoleJpaEntity> entities = springDataRoleRepository.findByNameIn(roleNames.stream().toList()).stream().collect(Collectors.toSet());
        if (entities.size() != roleNames.size()) {
            throw new IllegalStateException("One or more requested roles do not exist in the database");
        }
        return entities;
    }

    @Override
    public DomainList<User> findAll(int page, int size) {
        DomainList<User> result = new DomainList<>();
        for (UserJpaEntity entity : springDataUserRepository.findAll(PageRequest.of(page, size))) result.add(UserPersistenceMapper.toDomain(entity));
        return result;
    }

    @Override
    public DomainList<User> findByStatus(UserStatus status, int page, int size) {
        DomainList<User> result = new DomainList<>();
        for (UserJpaEntity entity : springDataUserRepository.findByStatus(UserJpaEntity.UserStatus.valueOf(status.name()), PageRequest.of(page, size))) result.add(UserPersistenceMapper.toDomain(entity));
        return result;
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
    public void deleteById(DomainId id) {
        springDataUserRepository.deleteById(UUID.fromString(id.value()));
    }
}

package com.appointymed.auth.domain.repository;

import com.appointymed.auth.domain.model.DomainId;
import com.appointymed.auth.domain.model.DomainList;
import com.appointymed.auth.domain.model.DomainOptional;
import com.appointymed.auth.domain.model.Email;
import com.appointymed.auth.domain.model.User;
import com.appointymed.auth.domain.model.UserStatus;

public interface UserRepository {
    DomainOptional<User> findById(DomainId id);

    DomainOptional<User> findByEmail(Email email);

    boolean existsByEmail(Email email);

    User save(User user);

    DomainList<User> findAll(int page, int size);

    DomainList<User> findByStatus(UserStatus status, int page, int size);

    long count();

    long countByStatus(UserStatus status);

    void deleteById(DomainId id);
}

package com.appointymed.auth.domain.repository;

import com.appointymed.auth.domain.model.Email;
import com.appointymed.auth.domain.model.User;
import com.appointymed.auth.domain.model.UserStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findById(UUID id);

    Optional<User> findByEmail(Email email);

    boolean existsByEmail(Email email);

    User save(User user);

    List<User> findAll(int page, int size);

    List<User> findByStatus(UserStatus status, int page, int size);

    long count();

    long countByStatus(UserStatus status);

    void deleteById(UUID id);
}
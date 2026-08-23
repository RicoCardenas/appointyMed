package com.appointymed.auth.domain.repository;

import com.appointymed.auth.domain.model.Role;

import java.util.List;
import java.util.Optional;

public interface RoleRepository {
    Optional<Role> findByName(String name);

    List<Role> findAll();

    List<Role> findByNames(List<String> names);
}
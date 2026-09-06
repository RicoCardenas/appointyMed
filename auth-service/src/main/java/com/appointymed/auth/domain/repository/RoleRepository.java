package com.appointymed.auth.domain.repository;

import com.appointymed.auth.domain.model.DomainList;
import com.appointymed.auth.domain.model.DomainOptional;
import com.appointymed.auth.domain.model.Role;

public interface RoleRepository {
    DomainOptional<Role> findByName(String name);

    DomainList<Role> findAll();

    DomainList<Role> findByNames(DomainList<String> names);
}

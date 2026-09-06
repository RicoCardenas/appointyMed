package com.appointymed.auth.domain.usecase;

import com.appointymed.auth.domain.exception.UserNotFoundException;
import com.appointymed.auth.domain.model.DomainId;
import com.appointymed.auth.domain.model.DomainSet;
import com.appointymed.auth.domain.model.Permission;
import com.appointymed.auth.domain.model.User;
import com.appointymed.auth.domain.repository.UserRepository;

public final class GetUserPermissionsUseCase {
    private final UserRepository userRepository;

    public GetUserPermissionsUseCase(UserRepository u) {
        userRepository = u;
    }

    public DomainSet<Permission> execute(DomainId id) {
        User u = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        DomainSet<Permission> result = DomainSet.empty();
        for (int i = 0; i < u.getRoles().size(); i++) {
            var permissions = u.getRoles().get(i).getPermissions();
            for (int j = 0; j < permissions.size(); j++) result = result.add(permissions.get(j));
        }
        return result;
    }
}

package com.appointymed.auth.application.service;

import com.appointymed.auth.application.dto.AssignRoleRequest;
import com.appointymed.auth.application.dto.RoleDto;
import com.appointymed.auth.application.dto.UserDto;
import com.appointymed.auth.application.dto.UpdateRolesRequest;
import com.appointymed.auth.application.mapper.UserMapper;
import com.appointymed.auth.domain.model.Role;
import com.appointymed.auth.domain.model.DomainId;
import com.appointymed.auth.domain.model.DomainInstant;
import com.appointymed.auth.domain.model.DomainSet;
import com.appointymed.auth.domain.model.User;
import com.appointymed.auth.domain.usecase.AssignRoleUseCase;
import com.appointymed.auth.domain.usecase.GetUserPermissionsUseCase;
import com.appointymed.auth.domain.usecase.RevokeRoleUseCase;
import com.appointymed.auth.domain.repository.RoleRepository;
import com.appointymed.auth.domain.repository.UserRepository;
import com.appointymed.auth.domain.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserManagementApplicationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AssignRoleUseCase assignRoleUseCase;
    private final RevokeRoleUseCase revokeRoleUseCase;
    private final GetUserPermissionsUseCase getUserPermissionsUseCase;
    private final UserMapper userMapper;
    private final com.appointymed.auth.domain.usecase.AuditOperationUseCase auditOperationUseCase;

    public UserManagementApplicationService(UserRepository userRepository,
                                            RoleRepository roleRepository,
                                            AssignRoleUseCase assignRoleUseCase,
                                            RevokeRoleUseCase revokeRoleUseCase,
                                            GetUserPermissionsUseCase getUserPermissionsUseCase,
                                            UserMapper userMapper,
                                            com.appointymed.auth.domain.usecase.AuditOperationUseCase auditOperationUseCase) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.assignRoleUseCase = assignRoleUseCase;
        this.revokeRoleUseCase = revokeRoleUseCase;
        this.getUserPermissionsUseCase = getUserPermissionsUseCase;
        this.userMapper = userMapper;
        this.auditOperationUseCase = auditOperationUseCase;
    }

    public UserDto assignRole(UUID userId, AssignRoleRequest request) {
        User user = assignRoleUseCase.execute(DomainId.of(userId.toString()), request.roleName());
        return userMapper.toDto(user);
    }

    public UserDto revokeRole(UUID userId, String roleName) {
        User user = revokeRoleUseCase.execute(DomainId.of(userId.toString()), roleName);
        return userMapper.toDto(user);
    }

    public UserDto replaceRoles(UUID targetUserId, UpdateRolesRequest request, UUID actorId, String ipAddress) {
        if (targetUserId.equals(actorId)) {
            throw new com.appointymed.auth.domain.exception.DomainException(
                    "A developer cannot modify their own roles", "SELF_ROLE_CHANGE_FORBIDDEN");
        }

        User actor = userRepository.findById(DomainId.of(actorId.toString()))
                .orElseThrow(() -> new UserNotFoundException(DomainId.of(actorId.toString())));
        User target = userRepository.findById(DomainId.of(targetUserId.toString()))
                .orElseThrow(() -> new UserNotFoundException(DomainId.of(targetUserId.toString())));

        DomainSet<Role> requestedRoles = DomainSet.empty();
        for (String roleName : request.roleNames()) requestedRoles = requestedRoles.add(Role.fromName(roleName));
        if (requestedRoles.size() == 0) {
            throw new com.appointymed.auth.domain.exception.DomainException(
                    "At least one role is required", "ROLE_REQUIRED");
        }

        boolean developer = actor.hasRole(Role.DEVELOPER) && !actor.hasRole(Role.ADMIN);
        if (developer && (target.hasRole(Role.ADMIN) || target.hasRole(Role.DEVELOPER)
                || requestedRoles.contains(Role.ADMIN) || requestedRoles.contains(Role.DEVELOPER))) {
            throw new com.appointymed.auth.domain.exception.DomainException(
                    "Developers cannot modify privileged users or assign privileged roles",
                    "ROLE_ESCALATION_FORBIDDEN");
        }

        User updated = User.builder()
                .id(target.getId())
                .email(target.getEmail())
                .password(target.getPassword())
                .fullName(target.getFullName())
                .phone(target.getPhone())
                .status(target.getStatus())
                .emailVerified(target.isEmailVerified())
                .roles(requestedRoles)
                .createdAt(target.getCreatedAt())
                .updatedAt(DomainInstant.ofEpochMillis(System.currentTimeMillis()))
                .build();
        User saved = userRepository.save(updated);

        auditOperationUseCase.execute(new com.appointymed.auth.domain.usecase.AuditOperationUseCase.AuditCommand(
                DomainId.of(actorId.toString()),
                "USER_ROLES_UPDATED",
                "USER",
                DomainId.of(targetUserId.toString()),
                rolesAsJson(target.getRoles()),
                rolesAsJson(requestedRoles),
                ipAddress
        ));
        return userMapper.toDto(saved);
    }

    private static String rolesAsJson(DomainSet<Role> roles) {
        StringBuilder result = new StringBuilder("[");
        for (int index = 0; index < roles.size(); index++) {
            if (index > 0) result.append(',');
            result.append('"').append(roles.get(index).name()).append('"');
        }
        return result.append(']').toString();
    }

    @Transactional(readOnly = true)
    public List<RoleDto> getAllRoles() {
        List<RoleDto> result = new java.util.ArrayList<>();
        var roles = roleRepository.findAll();
        for (int index = 0; index < roles.size(); index++) {
            Role role = roles.get(index);
            List<String> permissions = new java.util.ArrayList<>();
            for (int permissionIndex = 0; permissionIndex < role.getPermissions().size(); permissionIndex++) {
                permissions.add(role.getPermissions().get(permissionIndex).name());
            }
            result.add(new RoleDto(role.name(), role.name() + " role", permissions));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Set<String> getUserPermissions(UUID userId) {
        Set<String> result = new java.util.HashSet<>();
        var permissions = getUserPermissionsUseCase.execute(DomainId.of(userId.toString()));
        for (int index = 0; index < permissions.size(); index++) result.add(permissions.get(index).name());
        return result;
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(UUID userId) {
        User user = userRepository.findById(DomainId.of(userId.toString()))
                .orElseThrow(() -> new UserNotFoundException(DomainId.of(userId.toString())));
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers(int page, int size) {
        List<UserDto> result = new java.util.ArrayList<>();
        var users = userRepository.findAll(page, size);
        for (int index = 0; index < users.size(); index++) result.add(userMapper.toDto(users.get(index)));
        return result;
    }
}

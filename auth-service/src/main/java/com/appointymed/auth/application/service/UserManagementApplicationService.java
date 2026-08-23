package com.appointymed.auth.application.service;

import com.appointymed.auth.application.dto.AssignRoleRequest;
import com.appointymed.auth.application.dto.RoleDto;
import com.appointymed.auth.application.dto.UserDto;
import com.appointymed.auth.application.dto.UpdateRolesRequest;
import com.appointymed.auth.application.mapper.UserMapper;
import com.appointymed.auth.domain.model.Role;
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
        User user = assignRoleUseCase.execute(userId, request.roleName());
        return userMapper.toDto(user);
    }

    public UserDto revokeRole(UUID userId, String roleName) {
        User user = revokeRoleUseCase.execute(userId, roleName);
        return userMapper.toDto(user);
    }

    public UserDto replaceRoles(UUID targetUserId, UpdateRolesRequest request, UUID actorId, String ipAddress) {
        if (targetUserId.equals(actorId)) {
            throw new com.appointymed.auth.domain.exception.DomainException(
                    "A developer cannot modify their own roles", "SELF_ROLE_CHANGE_FORBIDDEN");
        }

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new UserNotFoundException(actorId));
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new UserNotFoundException(targetUserId));

        Set<Role> requestedRoles = request.roleNames().stream()
                .map(Role::fromName)
                .collect(Collectors.toSet());
        if (requestedRoles.isEmpty()) {
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
                .roles(Set.copyOf(requestedRoles))
                .createdAt(target.getCreatedAt())
                .updatedAt(java.time.Instant.now())
                .build();
        User saved = userRepository.save(updated);

        auditOperationUseCase.execute(new com.appointymed.auth.domain.usecase.AuditOperationUseCase.AuditCommand(
                actorId,
                "USER_ROLES_UPDATED",
                "USER",
                targetUserId,
                rolesAsJson(target.getRoles()),
                rolesAsJson(requestedRoles),
                ipAddress
        ));
        return userMapper.toDto(saved);
    }

    private static String rolesAsJson(Set<Role> roles) {
        return roles.stream()
                .map(Enum::name)
                .sorted()
                .map(role -> "\"" + role + "\"")
                .collect(Collectors.joining(",", "[", "]"));
    }

    @Transactional(readOnly = true)
    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(role -> new RoleDto(
                        role.name(),
                        role.name() + " role",
                        role.getPermissions().stream().map(Enum::name).toList()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public Set<String> getUserPermissions(UUID userId) {
        return getUserPermissionsUseCase.execute(userId).stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers(int page, int size) {
        return userRepository.findAll(page, size).stream()
                .map(userMapper::toDto)
                .toList();
    }
}
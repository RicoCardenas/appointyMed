package com.appointymed.auth.application.service;

import com.appointymed.auth.application.dto.AssignRoleRequest;
import com.appointymed.auth.application.dto.RoleDto;
import com.appointymed.auth.application.dto.UserDto;
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

    public UserManagementApplicationService(UserRepository userRepository,
                                            RoleRepository roleRepository,
                                            AssignRoleUseCase assignRoleUseCase,
                                            RevokeRoleUseCase revokeRoleUseCase,
                                            GetUserPermissionsUseCase getUserPermissionsUseCase,
                                            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.assignRoleUseCase = assignRoleUseCase;
        this.revokeRoleUseCase = revokeRoleUseCase;
        this.getUserPermissionsUseCase = getUserPermissionsUseCase;
        this.userMapper = userMapper;
    }

    public UserDto assignRole(UUID userId, AssignRoleRequest request) {
        User user = assignRoleUseCase.execute(userId, request.roleName());
        return userMapper.toDto(user);
    }

    public UserDto revokeRole(UUID userId, String roleName) {
        User user = revokeRoleUseCase.execute(userId, roleName);
        return userMapper.toDto(user);
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
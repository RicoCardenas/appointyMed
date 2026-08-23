package com.appointymed.auth.infrastructure.controller;

import com.appointymed.auth.application.dto.AssignRoleRequest;
import com.appointymed.auth.application.dto.RoleDto;
import com.appointymed.auth.application.dto.UserDto;
import com.appointymed.auth.application.dto.UpdateRolesRequest;
import com.appointymed.auth.application.service.UserManagementApplicationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserManagementController {
    private final UserManagementApplicationService userManagementService;

    public UserManagementController(UserManagementApplicationService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DEVELOPER' )")
    public ResponseEntity<List<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<UserDto> users = userManagementService.getAllUsers(page, size);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or #userId == authentication.principal")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID userId) {
        UserDto user = userManagementService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserDto> assignRole(@PathVariable UUID userId,
                                               @Valid @RequestBody AssignRoleRequest request) {
        UserDto user = userManagementService.assignRole(userId, request);
        return ResponseEntity.ok(user);
    }


    @PutMapping("/{userId}/roles")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_DEVELOPER')")
    public ResponseEntity<UserDto> replaceRoles(@PathVariable UUID userId,
                                                 @Valid @RequestBody UpdateRolesRequest request,
                                                 Authentication authentication,
                                                 jakarta.servlet.http.HttpServletRequest httpRequest) {
        UUID actorId = (UUID) authentication.getPrincipal();
        UserDto user = userManagementService.replaceRoles(userId, request, actorId, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{userId}/roles/{roleName}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserDto> revokeRole(@PathVariable UUID userId,
                                               @PathVariable String roleName) {
        UserDto user = userManagementService.revokeRole(userId, roleName);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{userId}/permissions")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or #userId == authentication.principal")
    public ResponseEntity<Set<String>> getUserPermissions(@PathVariable UUID userId) {
        Set<String> permissions = userManagementService.getUserPermissions(userId);
        return ResponseEntity.ok(permissions);
    }
}

@RestController
@RequestMapping("/api/v1/roles")
class RoleController {
    private final UserManagementApplicationService userManagementService;

    public RoleController(UserManagementApplicationService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        List<RoleDto> roles = userManagementService.getAllRoles();
        return ResponseEntity.ok(roles);
    }
}
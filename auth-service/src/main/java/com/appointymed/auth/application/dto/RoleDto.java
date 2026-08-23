package com.appointymed.auth.application.dto;

import java.util.List;

public record RoleDto(
        String name,
        String description,
        List<String> permissions
) {}
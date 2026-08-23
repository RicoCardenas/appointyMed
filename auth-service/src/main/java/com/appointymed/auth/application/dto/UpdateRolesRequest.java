package com.appointymed.auth.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateRolesRequest(
        @NotEmpty
        @Size(max = 4)
        List<@NotBlank String> roleNames
) {}

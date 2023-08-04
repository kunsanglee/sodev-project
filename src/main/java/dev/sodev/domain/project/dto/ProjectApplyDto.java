package dev.sodev.domain.project.dto;

import jakarta.validation.constraints.NotNull;

public record ProjectApplyDto(
        @NotNull
        String roleType
) {
}

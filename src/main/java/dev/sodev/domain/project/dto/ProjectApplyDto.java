package dev.sodev.domain.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Project Apply Dto")
public record ProjectApplyDto(
        @NotNull
        @Schema(description = "프로젝트 지원 역할(BE or FE)", example = "BE")
        String roleType
) {
}

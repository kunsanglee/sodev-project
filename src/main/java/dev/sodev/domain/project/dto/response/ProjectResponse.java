package dev.sodev.domain.project.dto.response;

import dev.sodev.domain.project.dto.ProjectDto;

import java.util.List;


public record ProjectResponse(
        String message,
        List<ProjectDto> project
) {
    public static ProjectResponse of (String message) {
        return new ProjectResponse(message, null);
    }

    public static ProjectResponse of (List<ProjectDto> project) {
        return new ProjectResponse(null, project);
    }
}

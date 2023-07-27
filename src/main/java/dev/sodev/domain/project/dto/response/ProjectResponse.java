package dev.sodev.domain.project.dto.response;

import dev.sodev.domain.project.dto.ProjectDto;

import java.util.List;


public record ProjectResponse(
        String message,
        ProjectDto project
) {
    public static ProjectResponse of (String message) {
        return new ProjectResponse(message, null);
    }

    public static ProjectResponse of (ProjectDto project) {
        return new ProjectResponse(null, project);
    }
}

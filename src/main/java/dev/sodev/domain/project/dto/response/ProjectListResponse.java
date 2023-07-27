package dev.sodev.domain.project.dto.response;

import dev.sodev.domain.project.dto.ProjectDto;

import java.util.List;


public record ProjectListResponse(
        String message,
        List<ProjectDto> project
) {
    public static ProjectListResponse of (String message) {
        return new ProjectListResponse(message, null);
    }

    public static ProjectListResponse of (List<ProjectDto> project) {
        return new ProjectListResponse(null, project);
    }
}

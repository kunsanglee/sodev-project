package dev.sodev.domain.project.dto.response;

import dev.sodev.domain.project.ProjectSkill;
import dev.sodev.domain.project.dto.projectDTO;
import dev.sodev.domain.project.dto.skillDTO;
import lombok.Getter;

import java.util.List;
import java.util.Map;


public record ProjectResponse(
        String message,
        List<projectDTO> project
) {
    public static ProjectResponse of (String message) {
        return new ProjectResponse(message, null);
    }

    public static ProjectResponse of (List<projectDTO> project) {
        return new ProjectResponse(null, project);
    }
}

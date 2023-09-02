package dev.sodev.domain.project.repository.query;

import dev.sodev.domain.project.Project;

import java.util.Optional;

public interface ProjectCustomRepository {
    Optional<Project> findProject(String nickName, String content);
}

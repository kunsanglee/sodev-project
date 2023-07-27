package dev.sodev.domain.project.repository;

import dev.sodev.domain.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long>, ProjectCustomRepository {

    void deleteProjectByCreatedBy(String email);

    List<Project> findAllByCreatedBy(String email);
}

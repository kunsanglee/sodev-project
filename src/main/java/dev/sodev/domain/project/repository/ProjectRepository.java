package dev.sodev.domain.project.repository;

import dev.sodev.domain.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long>, ProjectCustomRepository {

}

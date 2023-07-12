package dev.sodev.domain.project.repository;

import dev.sodev.domain.project.ProjectSkill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectSkillRepository extends JpaRepository<ProjectSkill, Long>, ProjectSkillCustomRepository {
    void deleteAllByProjectId(Long project_id);
}

package dev.sodev.domain.project.repository;

import dev.sodev.domain.project.ProjectSkill;
import dev.sodev.domain.project.repository.query.ProjectSkillCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ProjectSkillRepository extends JpaRepository<ProjectSkill, Long>, ProjectSkillCustomRepository {

    @Modifying
    @Query("delete from ProjectSkill s where s.project.id = :projectId")
    void deleteAllByProjectId(Long projectId);
}

package dev.sodev.domain.project.repository;

import dev.sodev.domain.project.dto.projectDTO;
import dev.sodev.domain.skill.Skill;

import java.util.List;

public interface ProjectSkillCustomRepository {
    List<projectDTO> findProject(Long projectId);
    void saveAll(List<Skill> skills, Long projectId);

}

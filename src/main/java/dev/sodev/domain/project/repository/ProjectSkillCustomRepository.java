package dev.sodev.domain.project.repository;

import dev.sodev.domain.project.dto.projectDTO;
import dev.sodev.domain.skill.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProjectSkillCustomRepository {
    List<projectDTO> findProject(Long projectId);
    void saveAll(List<Skill> skills, Long projectId);

    Page<projectDTO> searchAll(Pageable pageable);

    Page<projectDTO> searchFromEmail(String keyword, List<String> SkillSet,Pageable pageable);

    Page<projectDTO> searchFromTitle(String keyword, List<String> SkillSet,Pageable pageable);

    Page<projectDTO> searchFromContent(String keyword, List<String> SkillSet,Pageable pageable);

//    Page<projectDTO> searchFromNickname(String keyword, Pageable pageable);

    Page<projectDTO> searchFromSkill(List<String> skillSet, Pageable pageable);


}

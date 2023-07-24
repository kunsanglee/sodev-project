package dev.sodev.domain.project.repository;

import dev.sodev.domain.project.dto.ProjectDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProjectSkillCustomRepository {

    List<ProjectDto> findProject(Long projectId);
    void saveAll(List<Integer> skills, Long projectId);

    Page<ProjectDto> searchAll(Pageable pageable);

    Page<ProjectDto> searchFromEmail(String keyword, List<String> SkillSet, Pageable pageable);

    Page<ProjectDto> searchFromTitle(String keyword, List<String> SkillSet, Pageable pageable);

    Page<ProjectDto> searchFromContent(String keyword, List<String> SkillSet, Pageable pageable);

    Page<ProjectDto> searchFromNickname(String keyword,List<String> SkillSet, Pageable pageable);

    Page<ProjectDto> searchFromSkill(List<String> skillSet, Pageable pageable);


}

package dev.sodev.domain.project.service;

import dev.sodev.domain.enums.SearchType;
import dev.sodev.domain.project.dto.ProjectDto;
import dev.sodev.domain.project.dto.requset.ProjectInfoRequest;
import dev.sodev.domain.project.dto.response.ProjectResponse;
import dev.sodev.domain.skill.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProjectService {

    /**
     * 프로젝트 리스트 조회
     * 프로젝트 단건 조회
     * 프로젝트 생성
     * 프로젝트 수정
     * 프로젝트 삭제
     */

    List<ProjectResponse> projectList();

    ProjectResponse selectProject(Long projectId);

    ProjectResponse createProject(ProjectInfoRequest request);

    ProjectResponse updateProject(Long projectId,ProjectInfoRequest request);

    ProjectResponse deleteProject(Long projectId);

    List<Skill> findAndSaveSkill(List<String> skills);

    Page<ProjectDto> searchProject(SearchType searchType, String keyword, List<String> skillSet, Pageable pageable);


}

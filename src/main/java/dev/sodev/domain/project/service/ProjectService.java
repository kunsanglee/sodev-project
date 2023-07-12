package dev.sodev.domain.project.service;

import dev.sodev.domain.project.Project;
import dev.sodev.domain.project.dto.requset.ProjectInfoRequest;
import dev.sodev.domain.project.dto.requset.ProjectRequest;
import dev.sodev.domain.project.dto.response.ProjectResponse;
import dev.sodev.domain.skill.Skill;
import org.springframework.stereotype.Service;

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

    ProjectResponse selectProject(ProjectRequest request);

    ProjectResponse createProject(ProjectInfoRequest request);

    ProjectResponse updateProject(ProjectInfoRequest request);

    ProjectResponse deleteProject(ProjectRequest request);

    List<Skill> findAndSaveSkill(List<String> skills);


}

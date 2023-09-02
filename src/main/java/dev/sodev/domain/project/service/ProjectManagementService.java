package dev.sodev.domain.project.service;


import dev.sodev.domain.project.dto.ProjectApplyDto;
import dev.sodev.domain.member.dto.MemberProjectDto;
import dev.sodev.domain.project.dto.requset.PeerReviewRequest;
import dev.sodev.domain.project.dto.requset.ProjectInfoRequest;
import dev.sodev.domain.project.dto.response.ProjectResponse;

import java.util.List;

public interface ProjectManagementService {

    ProjectResponse createProject(ProjectInfoRequest request);

    ProjectResponse updateProject(Long projectId,ProjectInfoRequest request);

    ProjectResponse deleteProject(Long projectId);

    List<Integer> findAndSaveSkill(List<String> skills);

    void applyProject(Long projectId, ProjectApplyDto roleType); // 프로젝트 참여 지원하기

    void acceptApplicant(Long projectId, MemberProjectDto memberProjectDto); // 프로젝트 지원자 수락

    void declineApplicant(Long projectId, MemberProjectDto memberProjectDto); // 프로젝트 지원자 거절

    void kickMember(Long projectId, MemberProjectDto memberProjectDto); // 참여인원 내보내기

    void evaluationMembers(Long projectId, Long memberId, PeerReviewRequest request); // 프로젝트 완료 후 동료평가

    void startProject(Long projectId); // 프로젝트 시작

    void completeProject(Long projectId); // 프로젝트 종료
}

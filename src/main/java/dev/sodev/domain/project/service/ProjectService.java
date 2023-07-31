package dev.sodev.domain.project.service;


import dev.sodev.domain.likes.dto.LikesProjectDto;
import dev.sodev.domain.member.dto.MemberAppliedDto;
import dev.sodev.domain.member.dto.MemberHistoryDto;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import dev.sodev.domain.enums.SearchType;
import dev.sodev.domain.member.dto.MemberProjectDto;
import dev.sodev.domain.project.dto.ProjectDto;
import dev.sodev.domain.project.dto.requset.PeerReviewRequest;
import dev.sodev.domain.project.dto.requset.ProjectInfoRequest;
import dev.sodev.domain.project.dto.response.ProjectListResponse;
import dev.sodev.domain.project.dto.response.ProjectResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProjectService {

    /**
     * 프로젝트 리스트 조회
     * 프로젝트 단건 조회
     * 프로젝트 생성
     * 프로젝트 수정
     * 프로젝트 삭제
     */

    ProjectListResponse projectList();

    ProjectResponse selectProject(Long projectId);

    ProjectResponse createProject(ProjectInfoRequest request);

    ProjectResponse updateProject(Long projectId,ProjectInfoRequest request);

    ProjectResponse deleteProject(Long projectId);

    List<Integer> findAndSaveSkill(List<String> skills);

    Page<ProjectDto> searchProject(SearchType searchType, String keyword, List<String> skillSet, Pageable pageable);

    Slice<LikesProjectDto> likeProject(Long memberId, Pageable pageable);

    Slice<MemberAppliedDto> applyProject(Long memberId, Pageable pageable);

    Slice<MemberHistoryDto> projectHistory(Long memberId, Pageable pageable);

    void applyProject(Long projectId); // 프로젝트 참여 지원하기

    void acceptApplicant(Long projectId, MemberProjectDto memberProjectDto); // 프로젝트 지원자 수락

    void declineApplicant(Long projectId, MemberProjectDto memberProjectDto); // 프로젝트 지원자 거절

    void kickMember(Long projectId, MemberProjectDto memberProjectDto); // 참여인원 내보내기

    void evaluationMembers(Long memberId, PeerReviewRequest request); // 프로젝트 완료 후 동료평가

    void startProject(Long projectId); // 프로젝트 시작

    void completeProject(Long projectId); // 프로젝트 종료
}

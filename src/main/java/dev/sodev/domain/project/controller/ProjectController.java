package dev.sodev.domain.project.controller;

import dev.sodev.domain.enums.SearchType;
import dev.sodev.domain.likes.dto.response.LikeResponse;
import dev.sodev.domain.likes.service.LikeService;
import dev.sodev.domain.member.dto.MemberProjectDto;
import dev.sodev.domain.project.dto.ProjectApplyDto;
import dev.sodev.domain.project.dto.ProjectDto;
import dev.sodev.domain.project.dto.requset.PeerReviewRequest;
import dev.sodev.domain.project.dto.requset.ProjectInfoRequest;
import dev.sodev.domain.project.dto.response.ProjectResponse;
import dev.sodev.domain.project.service.ProjectManagementService;
import dev.sodev.domain.project.service.ProjectSearchService;
import dev.sodev.global.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Project", description = "프로젝트 api")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/projects")
public class ProjectController {

    private final ProjectManagementService projectManagementService;
    private final ProjectSearchService projectSearchService;
    private final LikeService likeService;

    @Operation(summary = "프로젝트 게시물 조회", description = "프로젝트 id로 해당 프로젝트 게시물을 조회합니다.")
    @GetMapping("/{projectId}")
    public Response<ProjectResponse> selectProject(@PathVariable Long projectId) {
        ProjectResponse selectProject = projectSearchService.selectProject(projectId);
        return Response.success(selectProject);
    }

    @Operation(summary = "프로젝트 작성", description = "백엔드, 프론트엔드 인원수, 프로젝트 시작, 종료, 모집기간, 프로젝트 제목, 내용, 사용기술, 작성자 본인의 직무를 입력하여 프로젝트 생성을 요청합니다.")
    @PostMapping
    public Response<ProjectResponse> createFeed(@RequestBody @Valid ProjectInfoRequest request) {
        ProjectResponse project = projectManagementService.createProject(request);
        return Response.success(project);
    }

    @Operation(summary = "프로젝트 수정", description = "프로젝트 id로 해당 프로젝트 게시물을 수정합니다.")
    @PutMapping("/{projectId}")
    public Response<ProjectResponse> updateFeed(@PathVariable Long projectId, @RequestBody @Valid ProjectInfoRequest request) {
        ProjectResponse project = projectManagementService.updateProject(projectId, request);
        return Response.success(project);
    }

    @Operation(summary = "프로젝트 삭제", description = "프로젝트 id로 해당 프로젝트 게시물을 삭제합니다.")
    @DeleteMapping("/{projectId}")
    public Response<ProjectResponse> deleteFeed(@PathVariable Long projectId) {
        ProjectResponse project = projectManagementService.deleteProject(projectId);
        return Response.success(project);
    }

    @Operation(summary = "프로젝트 참여 지원", description = "프로젝트 id와 지원 직무(BE or FE)를 입력하여 해당 프로젝트에 팀원으로 참여 지원합니다.")
    @PostMapping("/{projectId}/applies")
    public Response<ProjectResponse> applyProject(@PathVariable Long projectId, @RequestBody ProjectApplyDto roleType) {
        projectManagementService.applyProject(projectId, roleType);
        return Response.success();
    }

    @Operation(summary = "프로젝트 좋아요 누르기", description = "프로젝트 id로 해당 프로젝트 게시물을 좋아요 요청 합니다.")
    @PostMapping("/{projectId}/likes")
    public Response<LikeResponse> like(@PathVariable Long projectId) {
        LikeResponse response = likeService.like(projectId);
        return Response.success(response);
    }

    @Operation(summary = "프로젝트 검색", description = "프로젝트 제목, 내용, 작성자 이메일, 닉네임, 사용기술을 검색어로 해당하는 프로젝트의 리스트를 요청합니다.")
    @GetMapping("/search")
    public Slice<ProjectDto> searchAll(@RequestParam(required = false) String searchType,
                                       @RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) List<String> skillSet,
                                       Pageable pageable) {
        return projectSearchService.searchProject(SearchType.valueOf(searchType), keyword, skillSet, pageable);
    }

    @Operation(summary = "프로젝트 지원자 수락", description = "프로젝트 id와 지원자 요청 객체로 해당 지원자를 프로젝트 팀원으로 합류를 수락합니다.")
    @PostMapping("/{projectId}/applicants") // 프로젝트 지원자 수락
    public Response<Void> acceptApplicant(@PathVariable Long projectId, @RequestBody MemberProjectDto memberProjectDto) {
        projectManagementService.acceptApplicant(projectId, memberProjectDto);
        return Response.success();
    }

    @Operation(summary = "프로젝트 지원자 거절", description = "프로젝트 id와 지원자 요청 객체로 해당 지원자를 프로젝트 팀원으로 합류를 거절합니다.")
    @DeleteMapping("/{projectId}/applicants") // 프로젝트 지원자 거절
    public Response<Void> declineApplicant(@PathVariable Long projectId, @RequestBody MemberProjectDto memberProjectDto) {
        projectManagementService.declineApplicant(projectId, memberProjectDto);
        return Response.success();
    }

    @Operation(summary = "프로젝트 팀원 퇴장", description = "프로젝트 id와 합류 팀원 객체로 해당 팀원을 프로젝트 팀원에서 강제퇴장 요청합니다.")
    @PostMapping("/{projectId}/kicks")
    public Response<Void> kickMember(@PathVariable Long projectId, @RequestBody MemberProjectDto memberProjectDto) {
        projectManagementService.kickMember(projectId, memberProjectDto);
        return Response.success();
    }

    @Operation(summary = "프로젝트 팀원 동료평가 작성", description = "프로젝트 id와 평가 하려는 팀원 id, 평가 내용을 입력하여 동료평가를 작성합니다.")
    @PostMapping("/{projectId}/review/{memberId}")
    public Response<Void> projectReview(@PathVariable Long projectId, @PathVariable Long memberId, @RequestBody PeerReviewRequest request) {
        projectManagementService.evaluationMembers(projectId, memberId, request);
        return Response.success();
    }

    @Operation(summary = "프로젝트 시작", description = "프로젝트 id를 입력하여 모집중인 프로젝트를 시작합니다.")
    @PostMapping("/{projectId}/start")
    public Response<Void> startProject(@PathVariable Long projectId) {
        projectManagementService.startProject(projectId);
        return Response.success();
    }

    @Operation(summary = "프로젝트 종료", description = "프로젝트 id를 입력하여 진행중인 프로젝트를 종료합니다.")
    @PostMapping("/{projectId}/complete")
    public Response<Void> completeProject(@PathVariable Long projectId) {
        projectManagementService.completeProject(projectId);
        return Response.success();
    }
}

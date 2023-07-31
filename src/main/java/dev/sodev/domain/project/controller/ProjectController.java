package dev.sodev.domain.project.controller;

import dev.sodev.domain.enums.SearchType;

import dev.sodev.domain.likes.dto.response.LikeResponse;
import dev.sodev.domain.likes.service.LikeService;


import dev.sodev.domain.member.dto.MemberProjectDto;
import dev.sodev.domain.project.dto.ProjectDto;
import dev.sodev.domain.project.dto.requset.PeerReviewRequest;
import dev.sodev.domain.project.dto.requset.ProjectInfoRequest;
import dev.sodev.domain.project.dto.response.ProjectResponse;
import dev.sodev.domain.project.service.ProjectService;
import dev.sodev.global.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final LikeService likeService;

    @GetMapping("/{projectId}")
    public Response<ProjectResponse> selectProject(@PathVariable Long projectId) {
        ProjectResponse selectProject = projectService.selectProject(projectId);
        return Response.success(selectProject);
    }

    @PostMapping
    public Response<ProjectResponse> createFeed(@RequestBody @Valid ProjectInfoRequest request) {
        ProjectResponse project = projectService.createProject(request);
        return Response.success(project);
    }

    @PutMapping("/{projectId}")
    public Response<ProjectResponse> updateFeed(@PathVariable Long projectId, @RequestBody @Valid ProjectInfoRequest request) {
        ProjectResponse project = projectService.updateProject(projectId, request);
        return Response.success(project);
    }

    @DeleteMapping("/{projectId}")
    public Response<ProjectResponse> deleteFeed(@PathVariable Long projectId) {
        ProjectResponse project = projectService.deleteProject(projectId);
        return Response.success(project);
    }

    @PostMapping("/{projectId}/applies")
    public Response<ProjectResponse> applyProject(@PathVariable Long projectId) {
        projectService.applyProject(projectId);
        return Response.success();
    }

    @PostMapping("/{projectId}/likes")
    public Response<LikeResponse> like(@PathVariable Long projectId) {
        LikeResponse response = likeService.like(projectId);
        return Response.success(response);
    }

    @GetMapping("/search")
    public Page<ProjectDto> searchAll(@RequestParam(required = false) String searchType,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) List<String> skillSet,
                                      Pageable pageable) {
        return projectService.searchProject(SearchType.valueOf(searchType), keyword, skillSet, pageable);
    }

    @PostMapping("/{projectId}/applicants") // 프로젝트 지원자 수락
    public void acceptApplicant(@PathVariable Long projectId, @RequestBody MemberProjectDto memberProjectDto) {
        projectService.acceptApplicant(projectId, memberProjectDto);
    }

    @DeleteMapping("/{projectId}/applicants") // 프로젝트 지원자 거절
    public void declineApplicant(@PathVariable Long projectId, @RequestBody MemberProjectDto memberProjectDto) {
        projectService.declineApplicant(projectId, memberProjectDto);
    }

    @PostMapping("/{projectId}/kicks")
    public void kickMember(@PathVariable Long projectId, @RequestBody MemberProjectDto memberProjectDto) {
        projectService.kickMember(projectId, memberProjectDto);
    }

    @PostMapping("/review/{memberId}")
    public Response<Void> projectReview(@PathVariable Long memberId, @RequestBody PeerReviewRequest request) {
        projectService.evaluationMembers(memberId, request);
        return Response.success();
    }

    @PostMapping("/{projectId}/start")
    public void startProject(@PathVariable Long projectId) {
        projectService.startProject(projectId);
    }

    @PostMapping("/{projectId}/complete")
    public void completeProject(@PathVariable Long projectId) {
        projectService.completeProject(projectId);
    }
}

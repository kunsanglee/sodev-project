package dev.sodev.domain.project.controller;

import dev.sodev.domain.enums.SearchType;

import dev.sodev.domain.likes.dto.response.LikeResponse;
import dev.sodev.domain.likes.service.LikeService;


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

    @GetMapping("/search")
    public Page<ProjectDto> searchAll(@RequestParam(required = false) String searchType,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) List<String> skillSet,
                                      Pageable pageable) {
        return projectService.searchProject(SearchType.valueOf(searchType), keyword, skillSet, pageable);
    }
    @PostMapping("/{projectId}/likes")
    public Response<LikeResponse> like(@PathVariable Long projectId) {
        LikeResponse response = likeService.like(projectId);
        return Response.success(response);
    }

    @GetMapping("/{memberName}/likes")
    public Page<ProjectDto> likeProjectList(@PathVariable String memberName, Pageable pageable){
        return projectService.likeProject(memberName, pageable);
    }

    // 제안하기, 제안받은 프로젝트 빼기로함
//    @GetMapping("/{memberName}/offers")
//    public Page<ProjectDto> offerProject(@PathVariable String memberName){
//        return projectService.offerProject(memberName);
//    }

    @GetMapping("/{memberName}/applies")
    public Page<ProjectDto> applyProject(@PathVariable String memberName, Pageable pageable){
        return projectService.applyProject(memberName, pageable);
    }

    @GetMapping("/{memberName}/history")
    public Page<ProjectDto> projectHistory(@PathVariable String memberName, Pageable pageable){
        return projectService.projectHistory(memberName, pageable);
    }

    @PostMapping("/review/{memberId}")
    public Response<Void> projectReview(@PathVariable Long memberId,@RequestBody PeerReviewRequest request) {
        projectService.evaluationMembers(memberId, request);
        return Response.success();
    }

}

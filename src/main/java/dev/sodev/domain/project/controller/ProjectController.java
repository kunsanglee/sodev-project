package dev.sodev.domain.project.controller;

import dev.sodev.domain.enums.SearchType;
import dev.sodev.domain.project.dto.ProjectDto;
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

    @GetMapping("/search")
    public Page<ProjectDto> searchAll(@RequestParam(required = false) String searchType,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) List<String> skillSet,
                                      Pageable pageable) {
        return projectService.searchProject(SearchType.valueOf(searchType), keyword, skillSet, pageable);
    }
}

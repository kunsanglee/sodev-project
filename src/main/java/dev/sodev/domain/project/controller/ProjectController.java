package dev.sodev.domain.project.controller;

import dev.sodev.domain.project.dto.requset.ProjectInfoRequest;
import dev.sodev.domain.project.dto.requset.ProjectRequest;
import dev.sodev.domain.project.dto.response.ProjectResponse;
import dev.sodev.domain.project.dto.skillDTO;
import dev.sodev.domain.project.service.ProjectService;
import dev.sodev.global.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/projects")
    public Response<ProjectResponse> selectProject(@RequestBody ProjectRequest request) {
        ProjectResponse selectProject = projectService.selectProject(request);
        return Response.success(selectProject);
    }

    @PostMapping("/projects")
    public Response<ProjectResponse> createFeed(@RequestBody ProjectInfoRequest request) {
        ProjectResponse project = projectService.createProject(request);
        return Response.success(project);
    }

    @PutMapping("/projects")
    public Response<ProjectResponse> updateFeed(@RequestBody ProjectInfoRequest request) {
        ProjectResponse project = projectService.updateProject(request);
        return Response.success(project);
    }

    @DeleteMapping("/projects")
    public Response<ProjectResponse> deleteFeed(@RequestBody ProjectRequest request) {
        ProjectResponse project = projectService.deleteProject(request);
        return Response.success(project);
    }
}

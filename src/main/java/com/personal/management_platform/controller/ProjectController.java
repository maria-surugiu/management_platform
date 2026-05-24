package com.personal.management_platform.controller;

import com.personal.management_platform.dto.CreateProjectRequest;
import com.personal.management_platform.dto.ProjectResponse;
import com.personal.management_platform.dto.UpdateProjectRequest;
import com.personal.management_platform.model.User;
import com.personal.management_platform.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication) {

        String userId = (String) authentication.getPrincipal();

        UUID ownerId = UUID.fromString(userId);

        ProjectResponse createdProject = projectService.createProject(request, ownerId);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request,
            Authentication authentication) {

        String userId = (String) authentication.getPrincipal();
        UUID requesterId = UUID.fromString(userId);

        ProjectResponse updatedProject = projectService.updateProject(id, request, requesterId);

        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable UUID id,
            Authentication authentication) {

        String userId = (String) authentication.getPrincipal();
        UUID requesterId = UUID.fromString(userId);

        projectService.deleteProject(id, requesterId);

        return ResponseEntity.noContent().build(); // Status 204 No Content (Standardul pentru Delete cu succes)
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {

        List<ProjectResponse> projects = projectService.getAllActiveProjects();

        return ResponseEntity.ok(projects);
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<ProjectResponse> addMember(
            @PathVariable UUID id,
            @Valid @RequestBody com.personal.management_platform.dto.AddMemberRequest request,
            Authentication authentication) {

        String userIdStr = (String) authentication.getPrincipal();
        UUID requesterId = UUID.fromString(userIdStr);

        ProjectResponse updatedProject = projectService.addMemberToProject(id, request, requesterId);

        return ResponseEntity.ok(updatedProject);
    }
}
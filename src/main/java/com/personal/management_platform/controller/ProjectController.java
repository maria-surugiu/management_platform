package com.personal.management_platform.controller;

import com.personal.management_platform.dto.CreateProjectRequest;
import com.personal.management_platform.dto.ProjectResponse;
import com.personal.management_platform.model.User;
import com.personal.management_platform.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
}
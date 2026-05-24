package com.personal.management_platform.service;

import com.personal.management_platform.dto.AddMemberRequest;
import com.personal.management_platform.dto.CreateProjectRequest;
import com.personal.management_platform.dto.ProjectResponse;
import com.personal.management_platform.dto.UpdateProjectRequest;
import com.personal.management_platform.exception.ProjectNotFoundException;
import com.personal.management_platform.exception.UnauthorizedAccessException;
import com.personal.management_platform.exception.UserNotFoundException;
import com.personal.management_platform.model.Project;
import com.personal.management_platform.model.User;
import com.personal.management_platform.repository.ProjectRepository;
import com.personal.management_platform.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public ProjectResponse createProject(CreateProjectRequest request, UUID ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException("Owner not found in database"));

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwner(owner);

        Project savedProject = projectRepository.save(project);

        return mapToProjectResponse(savedProject);
    }

    private ProjectResponse mapToProjectResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getOwner().getId(),
                project.getOwner().getFirstName() + " " + project.getOwner().getLastName(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    @Transactional
    public ProjectResponse updateProject(UUID projectId, UpdateProjectRequest request, UUID requesterId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));

        if (!project.getOwner().getId().equals(requesterId)) {
            throw new UnauthorizedAccessException("You are not authorized to update this project");
        }

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }

        Project updatedProject = projectRepository.save(project);
        return mapToProjectResponse(updatedProject);
    }

    public void deleteProject(UUID projectId, UUID requesterId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));

        if (!project.getOwner().getId().equals(requesterId)) {
            throw new UnauthorizedAccessException("You are not authorized to delete this project");
        }

        project.setActive(false);
        projectRepository.save(project);
    }

    public List<ProjectResponse> getAllActiveProjects() {
        List<Project> projects = projectRepository.findByIsActiveTrue();

        return projects.stream()
                .map(this::mapToProjectResponse)
                .toList();
    }

    @Transactional
    public ProjectResponse addMemberToProject(UUID projectId, com.personal.management_platform.dto.AddMemberRequest request, UUID requesterId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));

        if (!project.getOwner().getId().equals(requesterId)) {
            throw new UnauthorizedAccessException("Only the project owner can add members");
        }

        if (request.getUserId().equals(requesterId)) {
            throw new IllegalArgumentException("You are the owner of this project, you cannot be added as a member.");
        }

        User newMember = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User to add not found in database"));

        if (project.getMembers().contains(newMember)) {
            throw new IllegalArgumentException("This user is already a member of the project.");
        }

        project.getMembers().add(newMember);

        Project updatedProject = projectRepository.save(project);
        return mapToProjectResponse(updatedProject);
    }
}
package com.personal.management_platform.service;

import com.personal.management_platform.dto.CreateProjectRequest;
import com.personal.management_platform.dto.ProjectResponse;
import com.personal.management_platform.dto.UpdateProjectRequest;
import com.personal.management_platform.exception.ProjectNotFoundException;
import com.personal.management_platform.exception.AccountDeactivatedException;
import com.personal.management_platform.exception.UnauthorizedAccessException;
import com.personal.management_platform.exception.UserNotFoundException;
import com.personal.management_platform.model.Project;
import com.personal.management_platform.model.User;
import com.personal.management_platform.repository.ProjectRepository;
import com.personal.management_platform.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, UUID ownerId) {
        logger.info("Create project attempt by userId={} projectName={}", ownerId, request.getName());
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> {
                    logger.warn("Create project failed: owner not found userId={}", ownerId);
                    return new UserNotFoundException("Owner not found in database");
                });

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwner(owner);

        Project savedProject = projectRepository.save(project);
        logger.info("Project created successfully: projectId={} name={} owner={}", savedProject.getId(), savedProject.getName(), ownerId);

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
        logger.info("Update project attempt: projectId={} requesterId={}", projectId, requesterId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    logger.warn("Update project failed: project not found projectId={}", projectId);
                    return new ProjectNotFoundException("Project not found");
                });

        if (!project.getOwner().getId().equals(requesterId)) {
            logger.warn("Update project failed: unauthorized access projectId={} requesterId={}", projectId, requesterId);
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
        logger.info("Project updated successfully: projectId={} status={}", projectId, updatedProject.getStatus());
        return mapToProjectResponse(updatedProject);
    }

    @Transactional
    public void deleteProject(UUID projectId, UUID requesterId) {
        logger.info("Delete project attempt: projectId={} requesterId={}", projectId, requesterId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    logger.warn("Delete project failed: project not found projectId={}", projectId);
                    return new ProjectNotFoundException("Project not found");
                });

        if (!project.getOwner().getId().equals(requesterId)) {
            logger.warn("Delete project failed: unauthorized access projectId={} requesterId={}", projectId, requesterId);
            throw new UnauthorizedAccessException("You are not authorized to delete this project");
        }

        project.setActive(false);
        projectRepository.save(project);
        logger.info("Project deleted (deactivated) successfully: projectId={}", projectId);
    }

    public List<ProjectResponse> getAllActiveProjects() {
        logger.debug("Fetching all active projects");
        List<Project> projects = projectRepository.findByIsActiveTrue();
        logger.info("Returning {} active projects", projects.size());

        return projects.stream()
                .map(this::mapToProjectResponse)
                .toList();
    }

    @Transactional
    public ProjectResponse addMemberToProject(UUID projectId, com.personal.management_platform.dto.AddMemberRequest request, UUID requesterId) {
        logger.info("Add member to project attempt: projectId={} newMemberId={} requesterId={}", projectId, request.getUserId(), requesterId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    logger.warn("Add member failed: project not found projectId={}", projectId);
                    return new ProjectNotFoundException("Project not found");
                });

        // Ensure project is active
        if (!project.isActive()) {
            logger.warn("Add member failed: project is not active projectId={}", projectId);
            throw new IllegalStateException("Cannot add members to an inactive project.");
        }

        if (!project.getOwner().getId().equals(requesterId)) {
            logger.warn("Add member failed: unauthorized access projectId={} requesterId={}", projectId, requesterId);
            throw new UnauthorizedAccessException("Only the project owner can add members");
        }

        if (request.getUserId().equals(requesterId)) {
            logger.warn("Add member failed: owner cannot be added as member projectId={} userId={}", projectId, requesterId);
            throw new IllegalArgumentException("You are the owner of this project, you cannot be added as a member.");
        }

        User newMember = userRepository.findById(request.getUserId())
                .orElseThrow(() -> {
                    logger.warn("Add member failed: user not found userId={}", request.getUserId());
                    return new UserNotFoundException("User to add not found in database");
                });

        // Ensure the user to add is active
        Boolean memberActive = newMember.getIsActive();
        if (memberActive == null || !memberActive) {
            logger.warn("Add member failed: user to add is not active userId={}", request.getUserId());
            throw new AccountDeactivatedException("Cannot add an inactive user to the project.");
        }

        if (project.getMembers().contains(newMember)) {
            logger.warn("Add member failed: user already member projectId={} userId={}", projectId, request.getUserId());
            throw new IllegalArgumentException("This user is already a member of the project.");
        }

        project.getMembers().add(newMember);

        Project updatedProject = projectRepository.save(project);
        logger.info("Member added successfully to project: projectId={} newMemberId={}", projectId, request.getUserId());
        return mapToProjectResponse(updatedProject);
    }

    public List<ProjectResponse> getMyProjects(UUID userId) {
        logger.debug("Fetching active projects for userId={}", userId);
        List<Project> projects = projectRepository.findActiveProjectsForUser(userId);
        logger.info("Returning {} projects for userId={}", projects.size(), userId);

        return projects.stream()
                .map(this::mapToProjectResponse)
                .toList();
    }
}
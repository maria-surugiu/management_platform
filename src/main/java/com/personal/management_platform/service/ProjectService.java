package com.personal.management_platform.service;

import com.personal.management_platform.dto.CreateProjectRequest;
import com.personal.management_platform.dto.ProjectResponse;
import com.personal.management_platform.exception.UserNotFoundException;
import com.personal.management_platform.model.Project;
import com.personal.management_platform.model.User;
import com.personal.management_platform.repository.ProjectRepository;
import com.personal.management_platform.repository.UserRepository;
import org.springframework.stereotype.Service;

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
}
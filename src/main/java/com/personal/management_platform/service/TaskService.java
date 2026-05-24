package com.personal.management_platform.service;

import com.personal.management_platform.dto.TaskRequest;
import com.personal.management_platform.dto.TaskResponse;
import com.personal.management_platform.exception.ProjectNotFoundException;
import com.personal.management_platform.exception.TaskNotFoundException;
import com.personal.management_platform.exception.UnauthorizedAccessException;
import com.personal.management_platform.exception.UserNotFoundException;
import com.personal.management_platform.model.Project;
import com.personal.management_platform.model.Task;
import com.personal.management_platform.model.User;
import com.personal.management_platform.repository.ProjectRepository;
import com.personal.management_platform.repository.TaskRepository;
import com.personal.management_platform.repository.TaskSpecifications;
import com.personal.management_platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TaskResponse createTask(UUID projectId, TaskRequest request, UUID requesterId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));

        validateUserInProject(project, requesterId);

        User creator = userRepository.findById(requesterId)
                .orElseThrow(() -> new UserNotFoundException("Creator not found"));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        task.setDeadline(request.getDeadline());
        task.setProject(project);
        task.setCreatedBy(creator);

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new UserNotFoundException("Assignee not found"));
            task.setAssignee(assignee);
        }

        Task savedTask = taskRepository.saveAndFlush(task);
        return mapToResponse(savedTask);
    }

    @Transactional
    public TaskResponse updateTask(UUID taskId, TaskRequest request, UUID requesterId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        validateUserInProject(task.getProject(), requesterId);

        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getDeadline() != null) task.setDeadline(request.getDeadline());

        Task updatedTask = taskRepository.saveAndFlush(task);
        return mapToResponse(updatedTask);
    }

    @Transactional
    public void deleteTask(UUID taskId, UUID requesterId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        validateUserInProject(task.getProject(), requesterId);

        taskRepository.delete(task);
    }

    @Transactional
    public TaskResponse assignTask(UUID taskId, UUID assigneeId, UUID requesterId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task with ID " + taskId + " not found"));

        validateUserInProject(task.getProject(), requesterId);

        if (assigneeId == null) {
            task.setAssignee(null); // Unassign
        } else {
            User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new UserNotFoundException("Assignee not found"));
            task.setAssignee(assignee);
        }

        return mapToResponse(taskRepository.saveAndFlush(task));
    }

    public List<TaskResponse> getFilteredTasks(UUID projectId, String status, String priority, UUID requesterId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));
        validateUserInProject(project, requesterId);

        return taskRepository.findAll(TaskSpecifications.filterTasks(projectId, status, priority))
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void validateUserInProject(Project project, UUID userId) {
        boolean isOwner = project.getOwner().getId().equals(userId);
        boolean isMember = project.getMembers().stream().anyMatch(m -> m.getId().equals(userId));

        if (!isOwner && !isMember) {
            throw new UnauthorizedAccessException("You must be a project member or owner to perform this action.");
        }
    }

    private TaskResponse mapToResponse(Task task) {
        UUID assigneeId = task.getAssignee() != null ? task.getAssignee().getId() : null;

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getStatus(),
                task.getDeadline(),
                task.getProject().getId(),
                assigneeId,
                task.getCreatedBy().getId(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
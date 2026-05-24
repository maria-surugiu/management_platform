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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TaskResponse createTask(UUID projectId, TaskRequest request, UUID requesterId) {
        logger.info("Create task attempt: projectId={} requesterId={} title={}", projectId, requesterId, request.getTitle());
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    logger.warn("Create task failed: project not found projectId={}", projectId);
                    return new ProjectNotFoundException("Project not found");
                });

        validateUserInProject(project, requesterId);

        User creator = userRepository.findById(requesterId)
                .orElseThrow(() -> {
                    logger.warn("Create task failed: creator not found userId={}", requesterId);
                    return new UserNotFoundException("Creator not found");
                });

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
                    .orElseThrow(() -> {
                        logger.warn("Create task failed: assignee not found assigneeId={}", request.getAssigneeId());
                        return new UserNotFoundException("Assignee not found");
                    });
            task.setAssignee(assignee);
        }

        Task savedTask = taskRepository.saveAndFlush(task);
        logger.info("Task created successfully: taskId={} projectId={} createdBy={}", savedTask.getId(), projectId, requesterId);
        return mapToResponse(savedTask);
    }

    @Transactional
    public TaskResponse updateTask(UUID taskId, TaskRequest request, UUID requesterId) {
        logger.info("Update task attempt: taskId={} requesterId={}", taskId, requesterId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    logger.warn("Update task failed: task not found taskId={}", taskId);
                    return new IllegalArgumentException("Task not found");
                });

        validateUserInProject(task.getProject(), requesterId);

        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getDeadline() != null) task.setDeadline(request.getDeadline());

        Task updatedTask = taskRepository.saveAndFlush(task);
        logger.info("Task updated successfully: taskId={} requesterId={}", taskId, requesterId);
        return mapToResponse(updatedTask);
    }

    @Transactional
    public void deleteTask(UUID taskId, UUID requesterId) {
        logger.info("Delete task attempt: taskId={} requesterId={}", taskId, requesterId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    logger.warn("Delete task failed: task not found taskId={}", taskId);
                    return new IllegalArgumentException("Task not found");
                });

        validateUserInProject(task.getProject(), requesterId);

        taskRepository.delete(task);
        logger.info("Task deleted successfully: taskId={} requesterId={}", taskId, requesterId);
    }

    @Transactional
    public TaskResponse assignTask(UUID taskId, UUID assigneeId, UUID requesterId) {
        logger.info("Assign task attempt: taskId={} assigneeId={} requesterId={}", taskId, assigneeId, requesterId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    logger.warn("Assign task failed: task not found taskId={}", taskId);
                    return new TaskNotFoundException("Task with ID " + taskId + " not found");
                });

        validateUserInProject(task.getProject(), requesterId);

        if (assigneeId == null) {
            task.setAssignee(null); // Unassign
            logger.info("Task unassigned: taskId={} by requesterId={}", taskId, requesterId);
        } else {
            User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> {
                        logger.warn("Assign task failed: assignee not found assigneeId={}", assigneeId);
                        return new UserNotFoundException("Assignee not found");
                    });
            task.setAssignee(assignee);
            logger.info("Task assigned: taskId={} assigneeId={} by requesterId={}", taskId, assigneeId, requesterId);
        }

        Task saved = taskRepository.saveAndFlush(task);
        return mapToResponse(saved);
    }

    public List<TaskResponse> getFilteredTasks(UUID projectId, String status, String priority, UUID requesterId) {
        logger.info("Get filtered tasks: projectId={} status={} priority={} requesterId={}", projectId, status, priority, requesterId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    logger.warn("Get filtered tasks failed: project not found projectId={}", projectId);
                    return new ProjectNotFoundException("Project not found");
                });
        validateUserInProject(project, requesterId);

        List<TaskResponse> results = taskRepository.findAll(TaskSpecifications.filterTasks(projectId, status, priority))
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        logger.info("Returning {} tasks for projectId={}", results.size(), projectId);
        return results;
    }

    private void validateUserInProject(Project project, UUID userId) {
        boolean isOwner = project.getOwner().getId().equals(userId);
        boolean isMember = project.getMembers().stream().anyMatch(m -> m.getId().equals(userId));
        if (!isOwner && !isMember) {
            logger.warn("Unauthorized access attempt: projectId={} userId={}", project.getId(), userId);
            throw new UnauthorizedAccessException("You must be a project member or owner to perform this action.");
        }
    }

    private TaskResponse mapToResponse(Task task) {
        UUID assigneeId = task.getAssignee() != null ? task.getAssignee().getId() : null;
        TaskResponse response = new TaskResponse(
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
        logger.debug("Mapped Task to TaskResponse: taskId={} assigneeId={}", task.getId(), assigneeId);
        return response;
    }
}
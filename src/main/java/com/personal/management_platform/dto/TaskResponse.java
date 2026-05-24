package com.personal.management_platform.dto;

import com.personal.management_platform.model.TaskPriority;
import com.personal.management_platform.model.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class TaskResponse {

    private UUID id;
    private String title;
    private String description;
    private TaskPriority priority;
    private TaskStatus status;
    private LocalDate deadline;
    private UUID projectId;
    private UUID assigneeId;
    private UUID createdById;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TaskResponse(UUID id, String title, String description, TaskPriority priority,
                        TaskStatus status, LocalDate deadline, UUID projectId,
                        UUID assigneeId, UUID createdById,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.deadline = deadline;
        this.projectId = projectId;
        this.assigneeId = assigneeId;
        this.createdById = createdById;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TaskPriority getPriority() { return priority; }
    public TaskStatus getStatus() { return status; }
    public LocalDate getDeadline() { return deadline; }
    public UUID getProjectId() { return projectId; }
    public UUID getAssigneeId() { return assigneeId; }
    public UUID getCreatedById() { return createdById; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
package com.personal.management_platform.controller;

import com.personal.management_platform.dto.TaskRequest;
import com.personal.management_platform.dto.TaskResponse;
import com.personal.management_platform.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable UUID projectId,
            @Valid @RequestBody TaskRequest request,
            Authentication authentication) {

        UUID requesterId = UUID.fromString((String) authentication.getPrincipal());
        TaskResponse response = taskService.createTask(projectId, request, requesterId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody TaskRequest request,
            Authentication authentication) {

        UUID requesterId = UUID.fromString((String) authentication.getPrincipal());
        return ResponseEntity.ok(taskService.updateTask(taskId, request, requesterId));
    }

    @PutMapping("/{taskId}/assignee")
    public ResponseEntity<TaskResponse> assignTask(
            @PathVariable UUID taskId,
            @RequestBody UUID assigneeId,
            Authentication authentication) {

        UUID requesterId = UUID.fromString((String) authentication.getPrincipal());
        return ResponseEntity.ok(taskService.assignTask(taskId, assigneeId, requesterId));
    }

    @DeleteMapping("/{taskId}/assignee")
    public ResponseEntity<TaskResponse> unassignTask(
            @PathVariable UUID taskId,
            Authentication authentication) {

        UUID requesterId = UUID.fromString((String) authentication.getPrincipal());
        return ResponseEntity.ok(taskService.assignTask(taskId, null, requesterId));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable UUID taskId,
            Authentication authentication) {

        UUID requesterId = UUID.fromString((String) authentication.getPrincipal());
        taskService.deleteTask(taskId, requesterId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<List<TaskResponse>> getFilteredTasks(
            @PathVariable UUID projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            Authentication authentication) {

        UUID requesterId = UUID.fromString((String) authentication.getPrincipal());
        List<TaskResponse> tasks = taskService.getFilteredTasks(projectId, status, priority, requesterId);

        return ResponseEntity.ok(tasks);
    }
}
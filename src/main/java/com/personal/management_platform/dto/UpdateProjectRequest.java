package com.personal.management_platform.dto;

import com.personal.management_platform.model.ProjectStatus;
import jakarta.validation.constraints.Size;

public class UpdateProjectRequest {

    @Size(max = 255, message = "Project name cannot exceed 255 characters")
    private String name;

    private String description;

    private ProjectStatus status;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }
}
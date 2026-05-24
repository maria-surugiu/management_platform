package com.personal.management_platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ChangeRoleRequest {

    @NotBlank(message = "Role cannot be blank")
    @Pattern(regexp = "^(USER|ADMIN)$", message = "Role must be either USER or ADMIN")
    private String role;

    public ChangeRoleRequest() {}

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
package com.personal.management_platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @NotBlank(message = "First name cannot be blank")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(max = 100)
    private String lastName;

    public UpdateProfileRequest() {}

    public UpdateProfileRequest(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Getters și Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
}
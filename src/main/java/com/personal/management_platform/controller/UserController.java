package com.personal.management_platform.controller;

import com.personal.management_platform.model.User;
import com.personal.management_platform.service.UserService;
import com.personal.management_platform.config.JwtUtil;
import com.personal.management_platform.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        return new ResponseEntity<>(userService.registerUser(registerRequest), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        User user = userService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());

        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest updateRequest) {

        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(userService.updateProfile(userId, updateRequest));
    }

    @PutMapping("/me/password")
    public ResponseEntity<String> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {

        UUID userId = UUID.fromString(authentication.getName());
        userService.changePassword(userId, request);

        return ResponseEntity.ok("Password saved successfully!");
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
package com.personal.management_platform.controller;

import com.personal.management_platform.model.User;
import com.personal.management_platform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Endpoint for registering new user
    // POST requests at http://localhost:8080/api/users/register
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@Valid @RequestBody User user) {
        User createdUser = userService.registerUser(user);

        // return new created object and the HTTP status -> 201 Created
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    // Endpoint for admin: kist all users
    // GET requests at http://localhost:8080/api/users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();

        // return list and -> 200 status
        return ResponseEntity.ok(users);
    }
}
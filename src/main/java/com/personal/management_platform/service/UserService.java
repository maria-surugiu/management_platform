package com.personal.management_platform.service;

import com.personal.management_platform.dto.ChangePasswordRequest;
import com.personal.management_platform.dto.UpdateProfileRequest;
import com.personal.management_platform.model.User;
import com.personal.management_platform.repository.UserRepository;
import com.personal.management_platform.exception.EmailAlreadyExistsException;
import com.personal.management_platform.exception.WeakPasswordException;
import com.personal.management_platform.exception.UserNotFoundException;
import com.personal.management_platform.exception.InvalidPasswordException;
import com.personal.management_platform.dto.RegisterRequest;
import com.personal.management_platform.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public UserResponse registerUser(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists!");
        }

        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$";

        if (!request.getPassword().matches(passwordRegex)) {
            throw new WeakPasswordException(
                    "Password must be at least 8 characters long, contain at least one digit, one uppercase letter, and one special character."
            );
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.setPasswordHash(encodedPassword);

        User savedUser = userRepository.save(user);

        return mapToUserResponse(savedUser);
    }

    public User authenticateUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Wrong email or password!"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidPasswordException("Wrong email or password!");
        }

        return user;
    }

    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found!"));

        return mapToUserResponse(user);
    }

    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found!"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        // Hibernate will automatically trigger @UpdateTimestamp for updatedAt
        User updatedUser = userRepository.save(user);

        return mapToUserResponse(updatedUser);
    }

    public void changePassword(UUID userId, ChangePasswordRequest request) {
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new InvalidPasswordException("New password cannot be identical to the current password!");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found!"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordException("Current password is incorrect!");
        }

        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$";
        if (!request.getNewPassword().matches(passwordRegex)) {
            throw new WeakPasswordException(
                    "Password must be at least 8 characters long, contain at least one digit, one uppercase letter, and one special character."
            );
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
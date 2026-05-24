package com.personal.management_platform.service;

import com.personal.management_platform.dto.*;
import com.personal.management_platform.exception.*;
import com.personal.management_platform.model.User;
import com.personal.management_platform.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

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
                user.getIsActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    @Transactional
    public UserResponse registerUser(RegisterRequest request) {
        logger.info("Register attempt for email={}", request.getEmail());
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("Registration failed: email already exists={}", request.getEmail());
            throw new EmailAlreadyExistsException("Email already exists!");
        }

        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$";

        if (!request.getPassword().matches(passwordRegex)) {
            logger.warn("Registration failed for email={}: weak password", request.getEmail());
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
        logger.info("User registered successfully: id={} email={}", savedUser.getId(), savedUser.getEmail());

        return mapToUserResponse(savedUser);
    }

    public User authenticateUser(String email, String password) {
        logger.info("Authentication attempt for email={}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Authentication failed for email={}: user not found", email);
                    return new UserNotFoundException("Wrong email or password!");
                });

        if (!user.getIsActive()) {
            logger.warn("Authentication failed for email={}: account deactivated", email);
            throw new AccountDeactivatedException("Your account has been deactivated. Please contact support.");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            logger.warn("Authentication failed for email={}: invalid password", email);
            throw new InvalidPasswordException("Wrong email or password!");
        }

        logger.info("Authentication successful for userId={} email={}", user.getId(), email);
        return user;
    }

    public UserResponse getUserById(UUID id) {
        logger.debug("Fetching user by id={}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User not found: id={}", id);
                    return new UserNotFoundException("User not found!");
                });

        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        logger.info("Updating profile for userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Update profile failed: user not found id={}", userId);
                    return new UserNotFoundException("User not found!");
                });

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        // Hibernate will automatically trigger @UpdateTimestamp for updatedAt
        User updatedUser = userRepository.save(user);
        logger.info("Profile updated for userId={}", userId);

        return mapToUserResponse(updatedUser);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        logger.info("Password change attempt for userId={}", userId);
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            logger.warn("Password change failed for userId={}: new password identical to current", userId);
            throw new InvalidPasswordException("New password cannot be identical to the current password!");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Password change failed: user not found id={}", userId);
                    return new UserNotFoundException("User not found!");
                });

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            logger.warn("Password change failed for userId={}: current password incorrect", userId);
            throw new InvalidPasswordException("Current password is incorrect!");
        }

        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$";
        if (!request.getNewPassword().matches(passwordRegex)) {
            logger.warn("Password change failed for userId={}: weak new password", userId);
            throw new WeakPasswordException(
                    "Password must be at least 8 characters long, contain at least one digit, one uppercase letter, and one special character."
            );
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        logger.info("Password changed successfully for userId={}", userId);
    }

    public List<UserResponse> getAllUsers() {
        logger.debug("Fetching all users");
        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(this::mapToUserResponse)
                .toList();
        logger.info("Returning {} users", users.size());
        return users;
    }

    @Transactional
    public UserResponse changeUserRole(UUID targetUserId, ChangeRoleRequest request) {
        logger.info("Change role attempt for userId={} to role={}", targetUserId, request.getRole());
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> {
                    logger.warn("Change role failed: user not found id={}", targetUserId);
                    return new UserNotFoundException("User not found!");
                });

        String oldRole = user.getRole();
        user.setRole(request.getRole());
        User updatedUser = userRepository.save(user);
        logger.info("Role changed for userId={} from {} to {}", targetUserId, oldRole, request.getRole());

        return mapToUserResponse(updatedUser);
    }

    @Transactional
    public UserResponse deactivateUser(UUID targetUserId) {
        logger.info("Deactivate user attempt for userId={}", targetUserId);
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> {
                    logger.warn("Deactivate failed: user not found id={}", targetUserId);
                    return new UserNotFoundException("User not found!");
                });

        user.setIsActive(false);
        User updatedUser = userRepository.save(user);
        logger.info("User deactivated: userId={}", targetUserId);

        return mapToUserResponse(updatedUser);
    }

    @Transactional
    public UserResponse reactivateUser(UUID targetUserId) {
        logger.info("Reactivate user attempt for userId={}", targetUserId);
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> {
                    logger.warn("Reactivate failed: user not found id={}", targetUserId);
                    return new UserNotFoundException("User not found!");
                });

        user.setIsActive(true);
        User updatedUser = userRepository.save(user);
        logger.info("User reactivated: userId={}", targetUserId);

        return mapToUserResponse(updatedUser);
    }

    public Optional<User> findByEmail(String email) {
        logger.debug("Looking up user by email={}", email);
        return userRepository.findByEmail(email);
    }
}
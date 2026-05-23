package com.personal.management_platform.service;

import com.personal.management_platform.model.User;
import com.personal.management_platform.repository.UserRepository;
import com.personal.management_platform.exception.EmailAlreadyExistsException;
import com.personal.management_platform.exception.WeakPasswordException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // register new user
    public User registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists!");
        }

        //String cleanPassword = user.getPasswordHash().trim();
        //user.setPasswordHash(cleanPassword);

        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$";

        if (!user.getPasswordHash().matches(passwordRegex)) {
            throw new WeakPasswordException(
                    "Password must be at least 8 characters long, contain at least one digit, one uppercase letter, and one special character."
            );
        }

        String encodedPassword = passwordEncoder.encode(user.getPasswordHash());
        user.setPasswordHash(encodedPassword);

        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // admin method: list all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}

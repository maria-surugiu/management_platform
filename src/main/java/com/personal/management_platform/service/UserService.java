package com.personal.management_platform.service;

import com.personal.management_platform.model.User;
import com.personal.management_platform.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // register new user
    public User registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email-ul already used!");
        }

        // TODO: encrypt password

        return userRepository.save(user);
    }

    // admin method: list all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}

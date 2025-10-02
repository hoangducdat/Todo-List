package org.project.backend.hubt.todo_list.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.backend.hubt.todo_list.dto.ChangePasswordRequest;
import org.project.backend.hubt.todo_list.dto.UpdateProfileRequest;
import org.project.backend.hubt.todo_list.dto.UserProfileResponse;
import org.project.backend.hubt.todo_list.entity.User;
import org.project.backend.hubt.todo_list.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;

    public UserProfileResponse getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setPhone(user.getPhone());
        response.setIsVerified(user.getIsVerified());
        response.setIsActive(user.getIsActive());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        return response;
    }

    @Transactional
    public String updateProfile(String currentUsername, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getUsername().equals(request.getUsername())) {
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new RuntimeException("Username is already taken");
            }
        }

        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email is already registered");
            }
            user.setIsVerified(false);
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());

        userRepository.save(user);

        if (!currentUsername.equals(request.getUsername())) {
            redisService.logoutUser(currentUsername);
        }

        log.info("Profile updated for user: {}", request.getUsername());
        return "Profile updated successfully";
    }

    @Transactional
    public String changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        redisService.logoutUser(username);

        log.info("Password changed for user: {}", username);
        return "Password changed successfully. Please login again.";
    }

    @Transactional
    public String deleteAccount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        redisService.logoutUser(username);

        userRepository.delete(user);

        log.info("Account deleted for user: {}", username);
        return "Account deleted successfully";
    }
}
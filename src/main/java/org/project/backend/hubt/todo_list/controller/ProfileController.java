package org.project.backend.hubt.todo_list.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.project.backend.hubt.todo_list.dto.ChangePasswordRequest;
import org.project.backend.hubt.todo_list.dto.UpdateProfileRequest;
import org.project.backend.hubt.todo_list.dto.UserProfileResponse;
import org.project.backend.hubt.todo_list.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(Authentication authentication) {
        String username = authentication.getName();
        UserProfileResponse profile = profileService.getUserProfile(username);
        return ResponseEntity.ok(profile);
    }

    @PutMapping
    public ResponseEntity<String> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        String currentUsername = authentication.getName();
        String message = profileService.updateProfile(currentUsername, request);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        String message = profileService.changePassword(username, request);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteAccount(Authentication authentication) {
        String username = authentication.getName();
        String message = profileService.deleteAccount(username);
        return ResponseEntity.ok(message);
    }
}
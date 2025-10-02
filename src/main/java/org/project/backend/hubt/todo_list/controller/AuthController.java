package org.project.backend.hubt.todo_list.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.project.backend.hubt.todo_list.dto.*;
import org.springframework.security.core.Authentication;
import org.project.backend.hubt.todo_list.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        String message = authService.register(request);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/verify")
    public ResponseEntity<AuthResponse> verifyAccount(@Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyAccount(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerificationOtp(@RequestParam String email) {
        String message = authService.resendVerificationOtp(email);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String message = authService.forgotPassword(request);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String message = authService.resetPassword(request);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(Authentication authentication) {
        authService.logout(authentication.getName());
        return ResponseEntity.ok("Logged out successfully");
    }
}
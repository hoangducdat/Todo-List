package org.project.backend.hubt.todo_list.service;

import lombok.RequiredArgsConstructor;
import org.project.backend.hubt.todo_list.dto.*;
import org.project.backend.hubt.todo_list.entity.User;
import org.project.backend.hubt.todo_list.exception.EmailException;
import org.project.backend.hubt.todo_list.exception.PasswordException;
import org.project.backend.hubt.todo_list.exception.UserException;
import org.project.backend.hubt.todo_list.repository.UserRepository;
import org.project.backend.hubt.todo_list.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final RedisService redisService;

    public String register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(false);
        user.setIsVerified(false);

        User savedUser = userRepository.save(user);

        otpService.sendVerificationOtp(savedUser.getEmail(), savedUser.getUsername());

        return "Registration successful. Please check your email for verification code.";
    }

    public AuthResponse verifyAccount(VerifyOtpRequest request) {
        if (!otpService.verifyOtp(request.getEmail(), request.getOtp(), OtpService.OtpType.VERIFICATION)) {
            throw new UserException("Invalid or expired OTP");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserException("User not found"));

        user.setIsVerified(true);
        user.setIsActive(true);
        User savedUser = userRepository.save(user);

        String token = jwtUtil.generateToken(savedUser.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(savedUser.getUsername());
        redisService.storeJwtToken(savedUser.getUsername(), token);
        redisService.storeRefreshToken(savedUser.getUsername(), refreshToken);

        return new AuthResponse(token, savedUser.getUsername(), savedUser.getEmail());
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .orElse(userRepository.findByEmail(request.getUsernameOrEmail())
                        .orElseThrow(() -> new UserException("User not found")));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new PasswordException("Invalid password");
        }

        if (!user.getIsActive()) {
            throw new UserException("Account is disabled");
        }

        if (!user.getIsVerified()) {
            throw new UserException("Account not verified. Please check your email for verification code.");
        }

        String token = jwtUtil.generateToken(user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        redisService.storeJwtToken(user.getUsername(), token);
        redisService.storeRefreshToken(user.getUsername(), refreshToken);

        return new AuthResponse(token, user.getUsername(), user.getEmail());
    }

    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserException("User not found with this email"));

        if (!user.getIsVerified()) {
            throw new UserException("Account not verified. Please verify your account first.");
        }

        otpService.sendPasswordResetOtp(user.getEmail(), user.getUsername());

        return "Password reset code sent to your email.";
    }

    public String resetPassword(ResetPasswordRequest request) {
        if (!otpService.verifyOtp(request.getEmail(), request.getOtp(), OtpService.OtpType.PASSWORD_RESET)) {
            throw new UserException("Invalid or expired OTP");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        redisService.logoutUser(user.getUsername());

        return "Password reset successfully. Please login with your new password.";
    }

    public void logout(String username) {
        redisService.logoutUser(username);
    }

    public String resendVerificationOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException("User not found with this email"));

        if (user.getIsVerified()) {
            throw new UserException("Account is already verified");
        }

        otpService.sendVerificationOtp(user.getEmail(), user.getUsername());
        return "Verification code resent to your email.";
    }
}
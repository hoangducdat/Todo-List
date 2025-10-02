package org.project.backend.hubt.todo_list.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    @Qualifier("customStringRedisTemplate")
    private final RedisTemplate<String, String> stringRedisTemplate;
    private final EmailService emailService;

    private static final String OTP_PREFIX = "otp:";
    private static final String RESET_PREFIX = "reset:";
    private static final Duration OTP_EXPIRATION = Duration.ofMinutes(3);
    private static final SecureRandom random = new SecureRandom();

    public String generateOtp() {
        int otp = 100000 + random.nextInt(900000); // 6-digit OTP
        return String.valueOf(otp);
    }

    public void sendVerificationOtp(String email, String username) {
        String otp = generateOtp();
        String key = OTP_PREFIX + "verify:" + email;

        stringRedisTemplate.opsForValue().set(key, otp, OTP_EXPIRATION);

        String subject = "Todo List - Account Verification";
        String body = buildVerificationEmailBody(username, otp);
        emailService.sendEmail(email, subject, body);

        log.info("Verification OTP sent to email: {}", email);
    }

    public void sendPasswordResetOtp(String email, String username) {
        String otp = generateOtp();
        String key = RESET_PREFIX + "password:" + email;

        stringRedisTemplate.opsForValue().set(key, otp, OTP_EXPIRATION);

        String subject = "Todo List - Password Reset";
        String body = buildPasswordResetEmailBody(username, otp);
        emailService.sendEmail(email, subject, body);

        log.info("Password reset OTP sent to email: {}", email);
    }

    public boolean verifyOtp(String email, String otp, OtpType type) {
        String key = switch (type) {
            case VERIFICATION -> OTP_PREFIX + "verify:" + email;
            case PASSWORD_RESET -> RESET_PREFIX + "password:" + email;
        };

        String storedOtp = stringRedisTemplate.opsForValue().get(key);

        if (storedOtp != null && storedOtp.equals(otp)) {
            stringRedisTemplate.delete(key);
            log.info("OTP verification successful for email: {}", email);
            return true;
        }

        log.warn("OTP verification failed for email: {}", email);
        return false;
    }

    public void deleteOtp(String email, OtpType type) {
        String key = switch (type) {
            case VERIFICATION -> OTP_PREFIX + "verify:" + email;
            case PASSWORD_RESET -> RESET_PREFIX + "password:" + email;
        };
        stringRedisTemplate.delete(key);
    }

    public boolean isOtpValid(String email, OtpType type) {
        String key = switch (type) {
            case VERIFICATION -> OTP_PREFIX + "verify:" + email;
            case PASSWORD_RESET -> RESET_PREFIX + "password:" + email;
        };
        return stringRedisTemplate.hasKey(key);
    }

    private String buildVerificationEmailBody(String username, String otp) {
        return String.format("""
            Dear %s,

            Welcome to Todo List! Please verify your account using the OTP below:

            Verification Code: %s

            This code will expire in 3 minutes.

            If you didn't request this verification, please ignore this email.

            Best regards,
            Todo List Team
            """, username, otp);
    }

    private String buildPasswordResetEmailBody(String username, String otp) {
        return String.format("""
            Dear %s,

            You have requested to reset your password for Todo List account.

            Reset Code: %s

            This code will expire in 3 minutes.

            If you didn't request this password reset, please ignore this email and your password will remain unchanged.

            Best regards,
            Todo List Team
            """, username, otp);
    }

    public enum OtpType {
        VERIFICATION, PASSWORD_RESET
    }
}
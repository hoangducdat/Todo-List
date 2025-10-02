package org.project.backend.hubt.todo_list.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Boolean isVerified;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
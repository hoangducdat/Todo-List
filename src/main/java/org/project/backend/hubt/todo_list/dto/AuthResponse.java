package org.project.backend.hubt.todo_list.dto;

import lombok.Data;

@Data
public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private String username;
    private String email;

    public AuthResponse(String token, String username, String email) {
        this.token = token;
        this.username = username;
        this.email = email;
    }
}
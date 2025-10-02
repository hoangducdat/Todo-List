package org.project.backend.hubt.todo_list.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private String colorCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer taskCount;
}
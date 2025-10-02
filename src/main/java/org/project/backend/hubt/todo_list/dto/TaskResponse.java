package org.project.backend.hubt.todo_list.dto;

import lombok.Data;
import org.project.backend.hubt.todo_list.entity.Task;

import java.time.LocalDateTime;

@Data
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private Boolean isCompleted;
    private Task.Status status;
    private Task.Priority priority;
    private LocalDateTime dueDate;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private CategoryResponse category;

    @Data
    public static class CategoryResponse {
        private Long id;
        private String name;
        private String colorCode;
    }
}
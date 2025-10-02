package org.project.backend.hubt.todo_list.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.project.backend.hubt.todo_list.entity.Task;

import java.time.LocalDateTime;

@Data
public class TaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(min = 1, max = 200, message = "Task title must be between 1 and 200 characters")
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private Task.Priority priority = Task.Priority.MEDIUM;

    private Task.Status status = Task.Status.TODO;

    private LocalDateTime dueDate;

    private Long categoryId;
}
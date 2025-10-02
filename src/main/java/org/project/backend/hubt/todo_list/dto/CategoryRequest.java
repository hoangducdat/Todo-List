package org.project.backend.hubt.todo_list.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 1, max = 100, message = "Category name must be between 1 and 100 characters")
    private String name;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    private String colorCode = "#007bff";
}
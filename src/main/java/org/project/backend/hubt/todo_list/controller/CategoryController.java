package org.project.backend.hubt.todo_list.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.project.backend.hubt.todo_list.dto.CategoryRequest;
import org.project.backend.hubt.todo_list.dto.CategoryResponse;
import org.project.backend.hubt.todo_list.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(Authentication authentication) {
        List<CategoryResponse> categories = categoryService.getAllCategories(authentication.getName());
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            Authentication authentication,
            @Valid @RequestBody CategoryRequest request) {

        CategoryResponse category = categoryService.createCategory(authentication.getName(), request);
        return ResponseEntity.ok(category);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {

        CategoryResponse category = categoryService.updateCategory(authentication.getName(), id, request);
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            Authentication authentication,
            @PathVariable Long id) {

        categoryService.deleteCategory(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
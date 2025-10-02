package org.project.backend.hubt.todo_list.service;

import lombok.RequiredArgsConstructor;
import org.project.backend.hubt.todo_list.dto.CategoryRequest;
import org.project.backend.hubt.todo_list.dto.CategoryResponse;
import org.project.backend.hubt.todo_list.entity.Category;
import org.project.backend.hubt.todo_list.entity.User;
import org.project.backend.hubt.todo_list.exception.UserException;
import org.project.backend.hubt.todo_list.repository.CategoryRepository;
import org.project.backend.hubt.todo_list.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public List<CategoryResponse> getAllCategories(String username) {
        User user = getUserByUsername(username);

        List<Category> categories = categoryRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return categories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse createCategory(String username, CategoryRequest request) {
        User user = getUserByUsername(username);

        if (categoryRepository.existsByNameAndUserId(request.getName(), user.getId())) {
            throw new UserException("Category name already exists");
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setColorCode(request.getColorCode());
        category.setUser(user);

        Category savedCategory = categoryRepository.save(category);
        return convertToResponse(savedCategory);
    }

    public CategoryResponse updateCategory(String username, Long categoryId, CategoryRequest request) {
        User user = getUserByUsername(username);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new UserException("Category not found"));

        if (!category.getUser().getId().equals(user.getId())) {
            throw new UserException("Category does not belong to user");
        }

        if (!category.getName().equals(request.getName()) &&
            categoryRepository.existsByNameAndUserId(request.getName(), user.getId())) {
            throw new UserException("Category name already exists");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setColorCode(request.getColorCode());

        Category savedCategory = categoryRepository.save(category);
        return convertToResponse(savedCategory);
    }

    public void deleteCategory(String username, Long categoryId) {
        User user = getUserByUsername(username);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new UserException("Category not found"));

        if (!category.getUser().getId().equals(user.getId())) {
            throw new UserException("Category does not belong to user");
        }

        categoryRepository.delete(category);
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserException("User not found"));
    }

    private CategoryResponse convertToResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setColorCode(category.getColorCode());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        response.setTaskCount(0); // Để tránh LazyInitializationException, set default = 0

        return response;
    }
}
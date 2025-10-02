package org.project.backend.hubt.todo_list.service;

import lombok.RequiredArgsConstructor;
import org.project.backend.hubt.todo_list.dto.TaskRequest;
import org.project.backend.hubt.todo_list.dto.TaskResponse;
import org.project.backend.hubt.todo_list.entity.Category;
import org.project.backend.hubt.todo_list.entity.Task;
import org.project.backend.hubt.todo_list.entity.User;
import org.project.backend.hubt.todo_list.exception.UserException;
import org.project.backend.hubt.todo_list.repository.CategoryRepository;
import org.project.backend.hubt.todo_list.repository.TaskRepository;
import org.project.backend.hubt.todo_list.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public List<TaskResponse> getAllTasks(String username, Long categoryId, Boolean isCompleted) {
        User user = getUserByUsername(username);

        List<Task> tasks = taskRepository.findTasksWithFilters(user.getId(), categoryId, isCompleted);

        return tasks.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public TaskResponse createTask(String username, TaskRequest request) {
        User user = getUserByUsername(username);

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setStatus(request.getStatus());
        task.setDueDate(request.getDueDate());
        task.setUser(user);

        task.setIsCompleted(request.getStatus() == Task.Status.COMPLETED);

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new UserException("Category not found"));

            if (!category.getUser().getId().equals(user.getId())) {
                throw new UserException("Category does not belong to user");
            }
            task.setCategory(category);
        }

        Task savedTask = taskRepository.save(task);
        return convertToResponse(savedTask);
    }

    public TaskResponse updateTask(String username, Long taskId, TaskRequest request) {
        User user = getUserByUsername(username);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new UserException("Task not found"));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new UserException("Task does not belong to user");
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setStatus(request.getStatus());
        task.setDueDate(request.getDueDate());

        task.setIsCompleted(request.getStatus() == Task.Status.COMPLETED);
        if (request.getStatus() == Task.Status.COMPLETED) {
            task.setCompletedAt(LocalDateTime.now());
        } else {
            task.setCompletedAt(null);
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new UserException("Category not found"));

            if (!category.getUser().getId().equals(user.getId())) {
                throw new UserException("Category does not belong to user");
            }
            task.setCategory(category);
        } else {
            task.setCategory(null);
        }

        Task savedTask = taskRepository.save(task);
        return convertToResponse(savedTask);
    }

    public TaskResponse toggleTaskCompletion(String username, Long taskId) {
        User user = getUserByUsername(username);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new UserException("Task not found"));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new UserException("Task does not belong to user");
        }

        task.setIsCompleted(!task.getIsCompleted());
        task.setCompletedAt(task.getIsCompleted() ? LocalDateTime.now() : null);

        Task savedTask = taskRepository.save(task);
        return convertToResponse(savedTask);
    }

    public TaskResponse updateTaskStatus(String username, Long taskId, Task.Status newStatus) {
        User user = getUserByUsername(username);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new UserException("Task not found"));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new UserException("Task does not belong to user");
        }

        task.setStatus(newStatus);
        task.setIsCompleted(newStatus == Task.Status.COMPLETED);

        if (newStatus == Task.Status.COMPLETED) {
            task.setCompletedAt(LocalDateTime.now());
        } else {
            task.setCompletedAt(null);
        }

        Task savedTask = taskRepository.save(task);
        return convertToResponse(savedTask);
    }

    public void deleteTask(String username, Long taskId) {
        User user = getUserByUsername(username);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new UserException("Task not found"));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new UserException("Task does not belong to user");
        }

        taskRepository.delete(task);
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserException("User not found"));
    }

    private TaskResponse convertToResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setIsCompleted(task.getIsCompleted());
        response.setStatus(task.getStatus());
        response.setPriority(task.getPriority());
        response.setDueDate(task.getDueDate());
        response.setCompletedAt(task.getCompletedAt());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());

        if (task.getCategory() != null) {
            TaskResponse.CategoryResponse categoryResponse = new TaskResponse.CategoryResponse();
            categoryResponse.setId(task.getCategory().getId());
            categoryResponse.setName(task.getCategory().getName());
            categoryResponse.setColorCode(task.getCategory().getColorCode());
            response.setCategory(categoryResponse);
        }

        return response;
    }
}
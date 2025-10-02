package org.project.backend.hubt.todo_list.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.project.backend.hubt.todo_list.dto.TaskRequest;
import org.project.backend.hubt.todo_list.dto.TaskResponse;
import org.project.backend.hubt.todo_list.entity.Task;
import org.project.backend.hubt.todo_list.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks(
            Authentication authentication,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean isCompleted) {

        List<TaskResponse> tasks = taskService.getAllTasks(
                authentication.getName(), categoryId, isCompleted);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            Authentication authentication,
            @Valid @RequestBody TaskRequest request) {

        TaskResponse task = taskService.createTask(authentication.getName(), request);
        return ResponseEntity.ok(task);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {

        TaskResponse task = taskService.updateTask(authentication.getName(), id, request);
        return ResponseEntity.ok(task);
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<TaskResponse> toggleTaskCompletion(
            Authentication authentication,
            @PathVariable Long id) {

        TaskResponse task = taskService.toggleTaskCompletion(authentication.getName(), id);
        return ResponseEntity.ok(task);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam String status) {

        try {
            Task.Status newStatus = Task.Status.valueOf(status.toUpperCase());
            TaskResponse task = taskService.updateTaskStatus(authentication.getName(), id, newStatus);
            return ResponseEntity.ok(task);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            Authentication authentication,
            @PathVariable Long id) {

        taskService.deleteTask(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
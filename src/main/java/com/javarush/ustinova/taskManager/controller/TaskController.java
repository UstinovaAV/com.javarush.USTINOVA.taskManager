package com.javarush.ustinova.taskManager.controller;

import com.javarush.ustinova.taskManager.dto.CreateTaskRequest;
import com.javarush.ustinova.taskManager.dto.TaskDto;
import com.javarush.ustinova.taskManager.dto.UserDto;
import com.javarush.ustinova.taskManager.entity.enums.TaskStatus;
import com.javarush.ustinova.taskManager.security.CustomUserDetails;
import com.javarush.ustinova.taskManager.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // в постмане: POST http://localhost:8080/api/tasks
    @PostMapping
    public ResponseEntity<TaskDto> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long userId = currentUser.getId();

        TaskDto createdTask = taskService.createTask(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }


    @GetMapping
    public ResponseEntity<List<TaskDto>> getTasks(
            @RequestParam(required = false) Long userId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        if (currentUser.isAdmin()) {
            // ЛОГИКА ДЛЯ АДМИНА:
            if (userId != null) {
                // Если админ указал userId, показываем задачи только этого пользователя
                return ResponseEntity.ok(taskService.getTasksByUser(userId));
            } else {
                // Если админ не указал userId, показываем вообще все задачи
                return ResponseEntity.ok(taskService.getAllTasks());
            }
        } else {
            // ЛОГИКА ДЛЯ ОБЫЧНОГО ПОЛЬЗОВАТЕЛЯ:
            // Если он пытается подставить чужой userId - блокируем!
            if (userId != null && !userId.equals(currentUser.getId())) {
                throw new AccessDeniedException("Вы можете просматривать только свои задачи");
            }

            // Иначе возвращаем его задачи (если userId не передан, берем его собственный ID)
            Long targetUserId = (userId != null) ? userId : currentUser.getId();
            return ResponseEntity.ok(taskService.getTasksByUser(targetUserId));
        }
    }


    @PutMapping("/{id}/status")
    public ResponseEntity<TaskDto> updateTaskStatus(
            @PathVariable("id") Long taskId,
            @RequestParam TaskStatus status) {

        return ResponseEntity.ok(taskService.updateTaskStatus(taskId, status));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTask(@PathVariable("id") Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build(); //  204 No Content
    }

//    @GetMapping
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<List<UserDto>> getAllUsers() {
//        return ResponseEntity.ok(userService.getAllUsers());
//    }
}
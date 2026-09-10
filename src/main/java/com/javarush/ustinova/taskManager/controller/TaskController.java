package com.javarush.ustinova.taskManager.controller;

import com.javarush.ustinova.taskManager.dto.CreateTaskRequest;
import com.javarush.ustinova.taskManager.dto.TaskDto;
import com.javarush.ustinova.taskManager.entity.enums.TaskStatus;
import com.javarush.ustinova.taskManager.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // в постмане: POST http://localhost:8080/api/tasks?userId=1
    @PostMapping
    public ResponseEntity<TaskDto> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            @RequestParam Long userId) { // Временно передаем userId в параметре (пока нет JWT)

        TaskDto createdTask = taskService.createTask(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    // в постмане:  GET http://localhost:8080/api/tasks?userId=1
    @GetMapping
    public ResponseEntity<List<TaskDto>> getTasksByUser(@RequestParam Long userId) {
        return ResponseEntity.ok(taskService.getTasksByUser(userId));
    }

    // в постмане:  PUT http://localhost:8080/api/tasks/1/status?status=IN_PROGRESS
    @PutMapping("/{id}/status")
    public ResponseEntity<TaskDto> updateTaskStatus(
            @PathVariable("id") Long taskId,
            @RequestParam TaskStatus status) {

        return ResponseEntity.ok(taskService.updateTaskStatus(taskId, status));
    }

    //в постмане:  DELETE http://localhost:8080/api/tasks/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable("id") Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build(); //  204 No Content
    }
}
package com.javarush.ustinova.taskManager.service;

import com.javarush.ustinova.taskManager.dto.CreateTaskRequest;
import com.javarush.ustinova.taskManager.dto.TaskDto;
import com.javarush.ustinova.taskManager.entity.Task;
import com.javarush.ustinova.taskManager.entity.User;
import com.javarush.ustinova.taskManager.entity.enums.TaskStatus;
import com.javarush.ustinova.taskManager.repository.TaskRepository;
import com.javarush.ustinova.taskManager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional
    public TaskDto createTask(CreateTaskRequest request, Long userId) {
        User user =userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь с id " + userId + " не найден"));


        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .deadline(request.deadline())
                .status(TaskStatus.TODO)
                .user(user)
                .build();

        Task savedTask = taskRepository.save(task);
        return mapToDto(savedTask);
    }

    public List<TaskDto> getTasksByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + userId + " не найден"));

        return taskRepository.findByUser(user).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional
    public TaskDto updateTaskStatus(Long taskId, TaskStatus newStatus) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача с ID " + taskId + " не найдена"));

        task.setStatus(newStatus);
        return mapToDto(task); // save не нужен, т.к. объект управляемый (managed), Hibernate сам обновит БД при коммите
    }

    @Transactional
    public void deleteTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new RuntimeException("Задача с ID " + taskId + " не найдена");
        }
        taskRepository.deleteById(taskId);
    }

    private TaskDto mapToDto(Task task) {
        return new TaskDto(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDeadline(),
                task.getStatus(),
                task.getUser().getId(),
                task.getUser().getUsername()
        );
    }
}
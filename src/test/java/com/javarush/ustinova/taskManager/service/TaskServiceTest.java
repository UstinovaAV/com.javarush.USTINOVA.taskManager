package com.javarush.ustinova.taskManager.service;

import com.javarush.ustinova.taskManager.entity.Task;
import com.javarush.ustinova.taskManager.entity.User;
import com.javarush.ustinova.taskManager.entity.enums.Role;
import com.javarush.ustinova.taskManager.entity.enums.TaskStatus;
import com.javarush.ustinova.taskManager.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void updateTaskStatus_Success() {
        User mockUser = User.builder().id(1L).username("testuser").role(Role.USER).build();
        Task task = Task.builder()
                .id(1L)
                .status(TaskStatus.TODO)
                .user(mockUser)
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.updateTaskStatus(1L, TaskStatus.DONE);

        // Assert (Проверка)
        assertEquals(TaskStatus.DONE, task.getStatus());
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void deleteTask_NotFound_ThrowsException() {
        when(taskRepository.existsById(99L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            taskService.deleteTask(99L);
        });

        assertEquals("Задача с ID 99 не найдена", exception.getMessage());
        verify(taskRepository, never()).deleteById(99L);
    }
}
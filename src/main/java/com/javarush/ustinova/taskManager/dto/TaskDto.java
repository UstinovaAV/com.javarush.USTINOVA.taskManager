package com.javarush.ustinova.taskManager.dto;

import com.javarush.ustinova.taskManager.entity.enums.TaskStatus;

import java.time.LocalDate;

public record TaskDto(
        Long id,
        String title,
        String description,
        LocalDate deadline,
        TaskStatus status,
        Long userId,
        String userName
) {
}

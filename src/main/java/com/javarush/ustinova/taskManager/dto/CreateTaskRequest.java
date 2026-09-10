package com.javarush.ustinova.taskManager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateTaskRequest(
        @NotBlank(message = "У задачи должно быть какое-то название")
        String title,

        String description,

        @NotNull(message = "Надо указать дедлайн")
        LocalDate deadline
) {
}

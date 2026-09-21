package com.javarush.ustinova.taskManager.dto;

import com.javarush.ustinova.taskManager.entity.enums.Role;

import java.time.LocalDateTime;

public record UserDto(
        Long id,
        String username,
        String email,
        Role role,
        boolean deleted,
        LocalDateTime deletedAt
) {}

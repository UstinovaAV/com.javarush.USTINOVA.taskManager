package com.javarush.ustinova.taskManager.dto;

import com.javarush.ustinova.taskManager.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(min = 3, max = 50) String username, // Можно не указывать
        @Email String email,                      // Можно не указывать
        @Size(min = 6) String password,           // Можно не указывать
        Role role                                 // Можно не указывать (и не получится, если не админ)
) {}
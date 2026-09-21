package com.javarush.ustinova.taskManager.controller;


import com.javarush.ustinova.taskManager.dto.UpdateUserRequest;
import com.javarush.ustinova.taskManager.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.javarush.ustinova.taskManager.dto.UserDto;
import com.javarush.ustinova.taskManager.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(
            @PathVariable("id") Long userId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // ПРОВЕРКА ПРАВ:
        // Если пользователь не админ и  пытается открыть не свой профиль - мы его блокируем
        if (!currentUser.isAdmin() && !currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("Доступ запрещен: вы можете просматривать только свой профиль");
        }

        UserDto user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable("id") Long userId,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        UserDto updatedUser = userService.updateUser(userId, request, currentUser);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Удалять может только админ
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long userId) {
        // Вызываем метод сервиса, который теперь делает Soft Delete
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build(); // Возвращаем 204 No Content
    }
}
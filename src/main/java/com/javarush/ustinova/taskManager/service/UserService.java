package com.javarush.ustinova.taskManager.service;


import com.javarush.ustinova.taskManager.dto.CreateUserRequest;
import com.javarush.ustinova.taskManager.dto.UpdateUserRequest;
import com.javarush.ustinova.taskManager.entity.enums.Role;
import com.javarush.ustinova.taskManager.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.javarush.ustinova.taskManager.dto.UserDto;
import com.javarush.ustinova.taskManager.entity.User;
import com.javarush.ustinova.taskManager.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDto registerUser(CreateUserRequest request) {
        log.info("Попытка регистрации нового пользователя: username={}", request.username());
        // Проверяем, не заняты ли логин и email
        if (userRepository.existsByUsername(request.username())) {
            log.warn("Ошибка регистрации: имя пользователя '{}' уже занято", request.username());
            throw new IllegalArgumentException("Имя пользователя уже занято");
        }
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Ошибка регистрации: email '{}' уже используется", request.email());
            throw new IllegalArgumentException("Email уже используется");
        }

        // Создаем пользователя (РОЛЬ ЖЕСТКО USER!)
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password())) // Хешируем пароль!
                .role(Role.USER)
                .build();

        //  Сохраняем и возвращаем DTO
        User savedUser = userRepository.save(user);
        log.info("Пользователь успешно зарегистрирован. ID={}, username={}", savedUser.getId(), savedUser.getUsername());

        return mapToDto(savedUser);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findByDeletedFalse().stream()
                .map(this::mapToDto)
                .toList();
    }

    public UserDto getUserById(Long userId) {
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> {
                    log.warn("Попытка получения несуществующего или удаленного пользователя с ID={}", userId);
                    throw new RuntimeException("Пользователь с ID " + userId + " не найден");
                });
        return mapToDto(user);
    }

    public UserDto updateUser(Long userId, UpdateUserRequest request, CustomUserDetails currentUser) {
        log.info("Попытка обновления профиля пользователя ID={} инициатором ID={}", userId, currentUser.getId());
        // Ищем пользователя
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Попытка обновления несуществующего пользователя с ID={}", userId);
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
                });

        // ПРОВЕРКА ПРАВ: Обычный юзер может менять только себя
        if (!currentUser.isAdmin() && !currentUser.getId().equals(userId)) {
            log.warn("Отказано в доступе: пользователь ID={} попытался изменить профиль пользователя ID={}", currentUser.getId(), userId);
            throw new AccessDeniedException("Вы можете редактировать только свой профиль");
        }

        // ПРОВЕРКА ПРАВ: Обычный юзер не может менять роль
        if (!currentUser.isAdmin() && request.role() != null) {
            log.warn("Пользователь ID={} попытался изменить роль", currentUser.getId());
            throw new AccessDeniedException("Только администратор может изменять роль");
        }

        // Обновляем поля (только те, которые переданы и не пустые)
        if (request.username() != null) {
            if (!user.getUsername().equals(request.username()) && userRepository.existsByUsername(request.username())) {
                log.warn("Ошибка обновления: новое имя пользователя '{}' уже занято", request.username());
                throw new IllegalArgumentException("Имя пользователя уже занято");
            }
            user.setUsername(request.username());
        }

        if (request.email() != null) {
            if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
                log.warn("Ошибка обновления: новый email '{}' уже используется", request.email());
                throw new IllegalArgumentException("Email уже используется");
            }
            user.setEmail(request.email());
        }

        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        // Роль меняем ТОЛЬКО если запрос сделал админ
        if (currentUser.isAdmin() && request.role() != null) {
            log.info("Администратор ID={} изменил роль пользователя ID={} на {}", currentUser.getId(), userId, request.role());
            user.setRole(request.role());
        }

        // Сохраняем и возвращаем
        User updatedUser = userRepository.save(user);
        log.info("Профиль пользователя ID={} успешно обновлен", userId);
        return mapToDto(updatedUser);
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Попытка удаления несуществующего пользователя с ID={}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
                });

        //  ПРОВЕРКА: Если это админ, убеждаемся, что он не последний
        if (user.getRole() == Role.ADMIN) {
            long activeAdminsCount = userRepository.countByRoleAndDeletedFalse(Role.ADMIN);
            if (activeAdminsCount <= 1) {
                log.error("ПОПЫТКА УДАЛИТЬ последнего активного администратора (ID={})", userId);
                throw new IllegalArgumentException("Нельзя удалить последнего активного администратора");
            }
        }

        // SOFT DELETE: Выполняется для ВСЕХ, кто прошел проверку выше
        user.markAsDeleted();
        userRepository.save(user);
        log.info("Пользователь ID={} (username={}) успешно помечен как удаленный (Soft Delete)", userId, user.getUsername());
    }

    private UserDto mapToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isDeleted(),
                user.getDeletedAt()
        );
    }
}

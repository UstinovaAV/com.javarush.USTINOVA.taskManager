package com.javarush.ustinova.taskManager.service;

import com.javarush.ustinova.taskManager.dto.CreateUserRequest;
import com.javarush.ustinova.taskManager.dto.UserDto;
import com.javarush.ustinova.taskManager.entity.enums.Role;
import com.javarush.ustinova.taskManager.entity.User;
import com.javarush.ustinova.taskManager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private CreateUserRequest validRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        validRequest = new CreateUserRequest("testuser", "test@example.com", "password123");

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("hashed_password")
                .role(Role.USER)
                .build();
    }

    @Test
    void registerUser_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDto result = userService.registerUser(validRequest);

        assertNotNull(result);
        assertEquals("testuser", result.username());
        assertEquals(Role.USER, result.role()); // Роль должна быть жестко USER

        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode("password123");
    }

    @Test
    void registerUser_UsernameExists_ThrowsException() {
        // Arrange: говорим, что имя уже занято
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(validRequest);
        });

        assertEquals("Имя пользователя уже занято", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_LastAdmin_ThrowsException() {
        User adminUser = User.builder().id(1L).role(Role.ADMIN).build();
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(adminUser));
        when(userRepository.countByRoleAndDeletedFalse(Role.ADMIN)).thenReturn(1L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.deleteUser(1L);
        });

        assertEquals("Нельзя удалить последнего активного администратора", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }
}
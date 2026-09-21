package com.javarush.ustinova.taskManager.config;

import com.javarush.ustinova.taskManager.entity.enums.Role;
import com.javarush.ustinova.taskManager.entity.Task;
import com.javarush.ustinova.taskManager.entity.enums.TaskStatus;
import com.javarush.ustinova.taskManager.entity.User;
import com.javarush.ustinova.taskManager.repository.TaskRepository;
import com.javarush.ustinova.taskManager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Override
    public void run(String... args) {
        // Заполняем БД только если она пустая
        if (userRepository.count() == 0) {

            // Создаем Админа
            User admin = User.builder()
                    .username("Юрий")
                    .email("admin@taskmanager.dev")
                    .password("{noop}admin123") // Пока текстом, потом будет зашифровано
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);

            // Создаем Обычного пользователя
            User user = User.builder()
                    .username("Анжелика Устинова")
                    .email("anzhelika@test.com")
                    .password("{noop}password123")
                    .role(Role.USER)
                    .build();
            userRepository.save(user);

            // Создаем тестовые задачи для Админа
            Task task1 = Task.builder()
                    .title("Подать показания счетчика холодной воды")
                    .description("Нужно своевременно подавать показания в УК")
                    .deadline(LocalDate.of(2026, 9, 23))
                    .status(TaskStatus.IN_PROGRESS)
                    .user(admin)
                    .build();
            taskRepository.save(task1);
            Task task2 = Task.builder()
                    .title("Подать показания счетчика горячей воды")
                    .description("Нужно своевременно подавать показания в Управляйку")
                    .deadline(LocalDate.of(2026, 9, 23))
                    .status(TaskStatus.IN_PROGRESS)
                    .user(admin)
                    .build();
            taskRepository.save(task2);

            // Создаем тестовые задачи для Пользователя
            Task task3 = Task.builder()
                    .title("Реализовать REST API")
                    .description("Написать контроллеры и сервисы для задач")
                    .deadline(LocalDate.of(2026, 9, 10))
                    .status(TaskStatus.DONE)
                    .user(user)
                    .build();
            taskRepository.save(task3);

            Task task4 = Task.builder()
                    .title("Реализовать авторизацию а аутентификацию к 14 сентября")
                    .description("Использовать Spring Security")
                    .deadline(LocalDate.of(2026, 9, 14))
                    .status(TaskStatus.IN_PROGRESS)
                    .user(user)
                    .build();
            taskRepository.save(task4);

            System.out.println("Тестовые данные успешно добавлены в БД!");
        }
    }
}
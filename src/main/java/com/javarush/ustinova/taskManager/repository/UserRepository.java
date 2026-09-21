package com.javarush.ustinova.taskManager.repository;

import com.javarush.ustinova.taskManager.entity.User;
import com.javarush.ustinova.taskManager.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // для soft delete
    Optional<User> findByUsernameAndDeletedFalse(String username);
    Optional<User> findByIdAndDeletedFalse(Long id);

    // посчитать количество активных админов, чтобы не удалить последнего
    long countByRoleAndDeletedFalse(Role role);

    //получить список только активных пользователей
    List<User> findByDeletedFalse();
}

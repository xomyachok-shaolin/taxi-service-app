package com.taxiservice.repository;

import com.taxiservice.model.Role;
import com.taxiservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link User}.
 * Предоставляет методы доступа к данным пользователей в базе данных.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Поиск пользователя по логину.
     *
     * @param username логин пользователя
     * @return Optional с пользователем или пустой
     */
    Optional<User> findByUsername(String username);

    /**
     * Проверка существования пользователя с данным логином.
     *
     * @param username логин
     * @return true если пользователь существует
     */
    boolean existsByUsername(String username);

    /**
     * Поиск пользователей по роли.
     *
     * @param role роль пользователя
     * @return список пользователей с указанной ролью
     */
    List<User> findByRole(Role role);

    /**
     * Поиск пользователей по части имени (без учёта регистра).
     *
     * @param name часть имени
     * @return список найденных пользователей
     */
    List<User> findByFullNameContainingIgnoreCase(String name);

    /**
     * Подсчёт количества пользователей с определённой ролью.
     *
     * @param role роль
     * @return количество пользователей
     */
    long countByRole(Role role);
}

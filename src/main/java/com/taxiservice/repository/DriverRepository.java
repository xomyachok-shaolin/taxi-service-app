package com.taxiservice.repository;

import com.taxiservice.model.Driver;
import com.taxiservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link Driver}.
 * Предоставляет методы доступа к профилям водителей.
 */
@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    /**
     * Поиск профиля водителя по связанному пользователю.
     *
     * @param user пользователь
     * @return Optional с профилем водителя
     */
    Optional<Driver> findByUser(User user);

    /**
     * Поиск профиля водителя по ID пользователя.
     *
     * @param userId ID пользователя
     * @return Optional с профилем водителя
     */
    Optional<Driver> findByUserId(Long userId);

    /**
     * Поиск водителей по статусу.
     *
     * @param status статус водителя (AVAILABLE, BUSY, OFFLINE)
     * @return список водителей с указанным статусом
     */
    List<Driver> findByStatus(String status);

    /**
     * Поиск водителей с рейтингом выше указанного.
     *
     * @param rating минимальный рейтинг
     * @return список водителей
     */
    List<Driver> findByRatingGreaterThanEqual(double rating);
}

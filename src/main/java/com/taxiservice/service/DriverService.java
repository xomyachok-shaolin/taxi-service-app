package com.taxiservice.service;

import com.taxiservice.model.Driver;
import com.taxiservice.model.User;
import com.taxiservice.repository.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления профилями водителей.
 * Обеспечивает создание, поиск и обновление данных водителей.
 *
 * @version 1.0
 */
@Service
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    /**
     * Создание профиля водителя.
     *
     * @param driver новый профиль водителя
     * @return сохранённый профиль
     */
    @Transactional
    public Driver save(Driver driver) {
        return driverRepository.save(driver);
    }

    /**
     * Поиск профиля водителя по ID.
     *
     * @param id идентификатор
     * @return Optional с профилем водителя
     */
    public Optional<Driver> findById(Long id) {
        return driverRepository.findById(id);
    }

    /**
     * Поиск профиля водителя по пользователю.
     *
     * @param user пользователь
     * @return Optional с профилем водителя
     */
    public Optional<Driver> findByUser(User user) {
        return driverRepository.findByUser(user);
    }

    /**
     * Поиск профиля водителя по ID пользователя.
     *
     * @param userId ID пользователя
     * @return Optional с профилем водителя
     */
    public Optional<Driver> findByUserId(Long userId) {
        return driverRepository.findByUserId(userId);
    }

    /**
     * Получение всех водителей.
     *
     * @return список всех водителей
     */
    public List<Driver> findAll() {
        return driverRepository.findAll();
    }

    /**
     * Получение водителей с определённым статусом.
     *
     * @param status статус (AVAILABLE, BUSY, OFFLINE)
     * @return список водителей
     */
    public List<Driver> findByStatus(String status) {
        return driverRepository.findByStatus(status);
    }

    /**
     * Получение свободных водителей.
     *
     * @return список доступных водителей
     */
    public List<Driver> findAvailable() {
        return driverRepository.findByStatus("AVAILABLE");
    }

    /**
     * Обновление статуса водителя.
     *
     * @param driverId  ID водителя
     * @param newStatus новый статус
     * @return обновлённый профиль
     */
    @Transactional
    public Driver updateStatus(Long driverId, String newStatus) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Водитель с ID " + driverId + " не найден"));
        driver.setStatus(newStatus);
        return driverRepository.save(driver);
    }

    /**
     * Удаление профиля водителя по ID.
     *
     * @param id идентификатор
     */
    @Transactional
    public void delete(Long id) {
        driverRepository.deleteById(id);
    }

    /**
     * Подсчёт общего количества водителей.
     *
     * @return количество водителей
     */
    public long count() {
        return driverRepository.count();
    }
}

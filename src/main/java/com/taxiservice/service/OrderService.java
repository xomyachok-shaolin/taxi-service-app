package com.taxiservice.service;

import com.taxiservice.model.*;
import com.taxiservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Сервис для управления заказами такси.
 * Реализует бизнес-логику создания, назначения и завершения заказов.
 *
 * @version 1.0
 */
@Service
public class OrderService {

    /** Базовый тариф за посадку (в рублях) */
    private static final BigDecimal BASE_FARE = new BigDecimal("150.00");

    /** Тариф за километр (в рублях) */
    private static final BigDecimal RATE_PER_KM = new BigDecimal("25.00");

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Создание нового заказа.
     * Автоматически рассчитывает расстояние и стоимость.
     *
     * @param order новый заказ
     * @return сохранённый заказ
     */
    @Transactional
    public Order createOrder(Order order) {
        order.setStatus(OrderStatus.CREATED);
        order.setOrderTime(LocalDateTime.now());

        // Моделирование расстояния (в реальной системе — API геокодирования)
        double distance = 3.0 + ThreadLocalRandom.current().nextDouble(17.0);
        distance = Math.round(distance * 10.0) / 10.0;
        order.setDistance(distance);

        // Расчёт стоимости
        BigDecimal price = BASE_FARE.add(
                RATE_PER_KM.multiply(BigDecimal.valueOf(distance))
        ).setScale(2, RoundingMode.HALF_UP);
        order.setPrice(price);

        return orderRepository.save(order);
    }

    /**
     * Назначение водителя на заказ.
     *
     * @param orderId  ID заказа
     * @param driver   назначаемый водитель
     * @return обновлённый заказ
     */
    @Transactional
    public Order assignDriver(Long orderId, Driver driver) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Заказ с ID " + orderId + " не найден"));
        order.setDriver(driver);
        order.setStatus(OrderStatus.ASSIGNED);
        return orderRepository.save(order);
    }

    /**
     * Обновление статуса заказа.
     * При статусе EN_ROUTE фиксирует время подачи.
     * При статусе COMPLETED фиксирует время завершения.
     *
     * @param orderId   ID заказа
     * @param newStatus новый статус
     * @return обновлённый заказ
     */
    @Transactional
    public Order updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Заказ с ID " + orderId + " не найден"));

        order.setStatus(newStatus);

        if (newStatus == OrderStatus.IN_PROGRESS && order.getPickupTime() == null) {
            order.setPickupTime(LocalDateTime.now());
        }
        if (newStatus == OrderStatus.COMPLETED && order.getCompletionTime() == null) {
            order.setCompletionTime(LocalDateTime.now());
        }

        return orderRepository.save(order);
    }

    /**
     * Поиск заказа по ID.
     *
     * @param id идентификатор
     * @return Optional с заказом
     */
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    /**
     * Получение всех заказов (новые первые).
     *
     * @return список заказов
     */
    public List<Order> findAll() {
        return orderRepository.findAllByOrderByOrderTimeDesc();
    }

    /**
     * Получение заказов клиента.
     *
     * @param clientId ID клиента
     * @return список заказов
     */
    public List<Order> findByClientId(Long clientId) {
        return orderRepository.findByClientIdOrderByOrderTimeDesc(clientId);
    }

    /**
     * Получение заказов водителя.
     *
     * @param driverId ID водителя
     * @return список заказов
     */
    public List<Order> findByDriverId(Long driverId) {
        return orderRepository.findByDriverIdOrderByOrderTimeDesc(driverId);
    }

    /**
     * Получение заказов по статусу.
     *
     * @param status статус заказа
     * @return список заказов
     */
    public List<Order> findByStatus(OrderStatus status) {
        return orderRepository.findByStatusOrderByOrderTimeDesc(status);
    }

    /**
     * Поиск заказов по адресу подачи.
     *
     * @param address часть адреса
     * @return список заказов
     */
    public List<Order> searchByAddress(String address) {
        return orderRepository.findByPickupAddressContainingIgnoreCaseOrderByOrderTimeDesc(address);
    }

    /**
     * Сортировка заказов по заданному критерию.
     *
     * @param orders   список заказов
     * @param sortBy   поле сортировки: date, price, distance, status
     * @param ascending направление сортировки
     * @return отсортированный список
     */
    public List<Order> sort(List<Order> orders, String sortBy, boolean ascending) {
        Comparator<Order> comparator;
        switch (sortBy) {
            case "price":
                comparator = Comparator.comparing(
                        o -> o.getPrice() != null ? o.getPrice() : BigDecimal.ZERO);
                break;
            case "distance":
                comparator = Comparator.comparing(
                        o -> o.getDistance() != null ? o.getDistance() : 0.0);
                break;
            case "status":
                comparator = Comparator.comparing(o -> o.getStatus().name());
                break;
            default:
                comparator = Comparator.comparing(Order::getOrderTime);
        }
        if (!ascending) {
            comparator = comparator.reversed();
        }
        orders.sort(comparator);
        return orders;
    }

    /**
     * Удаление заказа.
     *
     * @param id идентификатор заказа
     */
    @Transactional
    public void delete(Long id) {
        orderRepository.deleteById(id);
    }

    /**
     * Обновление заказа.
     *
     * @param order заказ
     * @return сохранённый заказ
     */
    @Transactional
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    /**
     * Подсчёт общего количества заказов.
     *
     * @return количество заказов
     */
    public long count() {
        return orderRepository.count();
    }

    /**
     * Подсчёт заказов по статусу.
     *
     * @param status статус
     * @return количество заказов
     */
    public long countByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }
}

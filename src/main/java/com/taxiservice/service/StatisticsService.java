package com.taxiservice.service;

import com.taxiservice.model.OrderStatus;
import com.taxiservice.model.Role;
import com.taxiservice.repository.OrderRepository;
import com.taxiservice.repository.UserRepository;
import com.taxiservice.repository.DriverRepository;
import org.springframework.stereotype.Service;

import com.taxiservice.model.Order;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Сервис статистики системы.
 * Вычисляет и предоставляет статистические данные о работе службы такси:
 * количество пользователей, среднее время ожидания, распределение заказов и т.д.
 *
 * @version 1.0
 */
@Service
public class StatisticsService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final OrderRepository orderRepository;

    public StatisticsService(UserRepository userRepository,
                             DriverRepository driverRepository,
                             OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * Общее количество пользователей системы.
     *
     * @return количество пользователей
     */
    public long getTotalUsers() {
        return userRepository.count();
    }

    /**
     * Количество клиентов.
     *
     * @return количество клиентов
     */
    public long getTotalClients() {
        return userRepository.countByRole(Role.CLIENT);
    }

    /**
     * Количество водителей.
     *
     * @return количество водителей
     */
    public long getTotalDrivers() {
        return driverRepository.count();
    }

    /**
     * Количество активных (доступных) водителей.
     *
     * @return количество доступных водителей
     */
    public long getAvailableDrivers() {
        return driverRepository.findByStatus("AVAILABLE").size();
    }

    /**
     * Общее количество заказов.
     *
     * @return количество заказов
     */
    public long getTotalOrders() {
        return orderRepository.count();
    }

    /**
     * Количество завершённых заказов.
     *
     * @return количество завершённых заказов
     */
    public long getCompletedOrders() {
        return orderRepository.countByStatus(OrderStatus.COMPLETED);
    }

    /**
     * Количество активных заказов (созданные, назначенные, в пути, в процессе).
     *
     * @return количество активных заказов
     */
    public long getActiveOrders() {
        return orderRepository.countByStatus(OrderStatus.CREATED)
                + orderRepository.countByStatus(OrderStatus.ASSIGNED)
                + orderRepository.countByStatus(OrderStatus.EN_ROUTE)
                + orderRepository.countByStatus(OrderStatus.IN_PROGRESS);
    }

    /**
     * Среднее время ожидания клиента (в минутах).
     *
     * @return среднее время ожидания или 0 если нет данных
     */
    public double getAverageWaitingTime() {
        List<Order> orders = orderRepository.findCompletedOrdersWithPickupTime();
        if (orders.isEmpty()) {
            return 0.0;
        }
        double totalMinutes = 0;
        for (Order order : orders) {
            totalMinutes += Duration.between(order.getOrderTime(), order.getPickupTime()).toMinutes();
        }
        double avg = totalMinutes / orders.size();
        return Math.round(avg * 10.0) / 10.0;
    }

    /**
     * Средняя стоимость поездки.
     *
     * @return средняя стоимость или 0
     */
    public double getAveragePrice() {
        Double avg = orderRepository.findAveragePrice();
        return avg != null ? Math.round(avg * 100.0) / 100.0 : 0.0;
    }

    /**
     * Среднее расстояние поездки (км).
     *
     * @return среднее расстояние или 0
     */
    public double getAverageDistance() {
        Double avg = orderRepository.findAverageDistance();
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    /**
     * Распределение заказов по статусам (для диаграммы).
     *
     * @return карта: статус -> количество
     */
    public Map<String, Long> getOrdersByStatus() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("Создан", orderRepository.countByStatus(OrderStatus.CREATED));
        stats.put("Назначен", orderRepository.countByStatus(OrderStatus.ASSIGNED));
        stats.put("В пути", orderRepository.countByStatus(OrderStatus.EN_ROUTE));
        stats.put("Выполняется", orderRepository.countByStatus(OrderStatus.IN_PROGRESS));
        stats.put("Завершён", orderRepository.countByStatus(OrderStatus.COMPLETED));
        stats.put("Отменён", orderRepository.countByStatus(OrderStatus.CANCELLED));
        return stats;
    }

    /**
     * Количество заказов за последние 7 дней (для гистограммы).
     *
     * @return карта: дата -> количество заказов
     */
    public Map<String, Long> getOrdersPerDay() {
        Map<String, Long> dailyStats = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();
            long count = orderRepository.countByOrderTimeBetween(start, end);
            dailyStats.put(date.toString(), count);
        }
        return dailyStats;
    }

    /**
     * Распределение пользователей по ролям (для диаграммы).
     *
     * @return карта: роль -> количество
     */
    public Map<String, Long> getUsersByRole() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("Клиенты", userRepository.countByRole(Role.CLIENT));
        stats.put("Водители", userRepository.countByRole(Role.DRIVER));
        stats.put("Диспетчеры", userRepository.countByRole(Role.DISPATCHER));
        stats.put("Администраторы", userRepository.countByRole(Role.ADMIN));
        return stats;
    }
}

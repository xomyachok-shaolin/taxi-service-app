package com.taxiservice.repository;

import com.taxiservice.model.Order;
import com.taxiservice.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Репозиторий для работы с сущностью {@link Order}.
 * Предоставляет методы для управления заказами такси.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Поиск заказов по ID клиента.
     *
     * @param clientId ID клиента
     * @return список заказов клиента
     */
    List<Order> findByClientIdOrderByOrderTimeDesc(Long clientId);

    /**
     * Поиск заказов по ID водителя.
     *
     * @param driverId ID водителя
     * @return список заказов водителя
     */
    List<Order> findByDriverIdOrderByOrderTimeDesc(Long driverId);

    /**
     * Поиск заказов по статусу.
     *
     * @param status статус заказа
     * @return список заказов
     */
    List<Order> findByStatusOrderByOrderTimeDesc(OrderStatus status);

    /**
     * Поиск заказов за определённый период.
     *
     * @param start начало периода
     * @param end   конец периода
     * @return список заказов
     */
    List<Order> findByOrderTimeBetweenOrderByOrderTimeDesc(LocalDateTime start, LocalDateTime end);

    /**
     * Подсчёт заказов по статусу.
     *
     * @param status статус заказа
     * @return количество заказов
     */
    long countByStatus(OrderStatus status);

    /**
     * Поиск заказов по адресу подачи (без учёта регистра).
     *
     * @param address часть адреса
     * @return список заказов
     */
    List<Order> findByPickupAddressContainingIgnoreCaseOrderByOrderTimeDesc(String address);

    /**
     * Вычисление среднего времени ожидания (в минутах) для завершённых заказов.
     *
     * @return среднее время ожидания
     */
    /**
     * Поиск завершённых заказов, у которых есть время подачи (для расчёта среднего ожидания).
     *
     * @return список завершённых заказов с pickupTime
     */
    @Query("SELECT o FROM Order o WHERE o.pickupTime IS NOT NULL AND o.status = com.taxiservice.model.OrderStatus.COMPLETED")
    List<Order> findCompletedOrdersWithPickupTime();

    /**
     * Вычисление средней стоимости завершённых заказов.
     *
     * @return средняя стоимость
     */
    @Query("SELECT AVG(o.price) FROM Order o WHERE o.status = 'COMPLETED' AND o.price IS NOT NULL")
    Double findAveragePrice();

    /**
     * Вычисление среднего расстояния завершённых заказов.
     *
     * @return среднее расстояние
     */
    @Query("SELECT AVG(o.distance) FROM Order o WHERE o.status = 'COMPLETED' AND o.distance IS NOT NULL")
    Double findAverageDistance();

    /**
     * Подсчёт заказов за определённый период.
     *
     * @param start начало периода
     * @param end   конец периода
     * @return количество заказов
     */
    long countByOrderTimeBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Поиск всех заказов, отсортированных по дате (новые первые).
     *
     * @return список заказов
     */
    List<Order> findAllByOrderByOrderTimeDesc();

    /**
     * Поиск активных заказов водителя (не завершённых и не отменённых).
     *
     * @param driverId ID водителя
     * @param statuses список статусов
     * @return список активных заказов
     */
    List<Order> findByDriverIdAndStatusIn(Long driverId, List<OrderStatus> statuses);
}

package com.taxiservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Сущность заказа такси.
 * Содержит всю информацию о поездке: адреса, время, стоимость, статус.
 * Связана с клиентом (User) и водителем (Driver).
 *
 * @version 1.0
 */
@Entity
@Table(name = "orders")
public class Order {

    /** Уникальный идентификатор заказа */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Клиент, оформивший заказ */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    /** Водитель, назначенный на заказ */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    /** Адрес подачи автомобиля */
    @NotBlank(message = "Адрес подачи не может быть пустым")
    @Column(name = "pickup_address", nullable = false)
    private String pickupAddress;

    /** Адрес назначения */
    @NotBlank(message = "Адрес назначения не может быть пустым")
    @Column(name = "destination_address", nullable = false)
    private String destinationAddress;

    /** Текущий статус заказа */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    /** Дата и время создания заказа */
    @Column(name = "order_time", nullable = false, updatable = false)
    private LocalDateTime orderTime;

    /** Дата и время подачи автомобиля */
    @Column(name = "pickup_time")
    private LocalDateTime pickupTime;

    /** Дата и время завершения поездки */
    @Column(name = "completion_time")
    private LocalDateTime completionTime;

    /** Расстояние поездки в километрах */
    private Double distance;

    /** Стоимость поездки в рублях */
    private BigDecimal price;

    /** Комментарий клиента к заказу */
    @Column(length = 500)
    private String comment;

    public Order() {
        this.orderTime = LocalDateTime.now();
        this.status = OrderStatus.CREATED;
    }

    /**
     * Конструктор с основными параметрами.
     *
     * @param client             клиент
     * @param pickupAddress      адрес подачи
     * @param destinationAddress адрес назначения
     */
    public Order(User client, String pickupAddress, String destinationAddress) {
        this();
        this.client = client;
        this.pickupAddress = pickupAddress;
        this.destinationAddress = destinationAddress;
    }

    /**
     * Вычисляет время ожидания клиента (от создания заказа до подачи автомобиля).
     *
     * @return время ожидания в минутах, или null если подача ещё не состоялась
     */
    public Long getWaitingTimeMinutes() {
        if (pickupTime == null) {
            return null;
        }
        return Duration.between(orderTime, pickupTime).toMinutes();
    }

    /**
     * Вычисляет продолжительность поездки.
     *
     * @return продолжительность в минутах, или null если поездка не завершена
     */
    public Long getTripDurationMinutes() {
        if (pickupTime == null || completionTime == null) {
            return null;
        }
        return Duration.between(pickupTime, completionTime).toMinutes();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getClient() {
        return client;
    }

    public void setClient(User client) {
        this.client = client;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }

    public LocalDateTime getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(LocalDateTime pickupTime) {
        this.pickupTime = pickupTime;
    }

    public LocalDateTime getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(LocalDateTime completionTime) {
        this.completionTime = completionTime;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

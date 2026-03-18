package com.taxiservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность профиля водителя.
 * Содержит информацию о водительском удостоверении, рейтинге и статусе.
 * Связана с пользователем (один к одному) и транспортными средствами (один ко многим).
 *
 * @version 1.0
 */
@Entity
@Table(name = "drivers")
public class Driver {

    /** Уникальный идентификатор профиля водителя */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Связанный пользователь системы */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** Номер водительского удостоверения */
    @NotBlank(message = "Номер удостоверения не может быть пустым")
    @Column(name = "license_number", nullable = false)
    private String licenseNumber;

    /** Средний рейтинг водителя (от 0.0 до 5.0) */
    @Column(nullable = false)
    private double rating = 5.0;

    /** Текущий статус водителя: AVAILABLE, BUSY, OFFLINE */
    @Column(nullable = false)
    private String status = "OFFLINE";

    /** Список транспортных средств водителя */
    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Vehicle> vehicles = new ArrayList<>();

    /** Список заказов, назначенных водителю */
    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();

    public Driver() {
    }

    /**
     * Конструктор с основными параметрами.
     *
     * @param user          связанный пользователь
     * @param licenseNumber номер водительского удостоверения
     */
    public Driver(User user, String licenseNumber) {
        this.user = user;
        this.licenseNumber = licenseNumber;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}

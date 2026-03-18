package com.taxiservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность пользователя системы.
 * Хранит учётные данные, личную информацию и роль пользователя.
 * Связана с заказами (для клиентов) и профилем водителя (для водителей).
 *
 * @version 1.0
 */
@Entity
@Table(name = "users")
public class User {

    /** Уникальный идентификатор пользователя */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Логин пользователя (уникальный) */
    @NotBlank(message = "Логин не может быть пустым")
    @Size(min = 3, max = 50, message = "Логин должен содержать от 3 до 50 символов")
    @Column(unique = true, nullable = false)
    private String username;

    /** Хешированный пароль пользователя */
    @NotBlank(message = "Пароль не может быть пустым")
    @Column(nullable = false)
    private String password;

    /** Полное имя пользователя */
    @NotBlank(message = "ФИО не может быть пустым")
    @Size(max = 100)
    @Column(name = "full_name", nullable = false)
    private String fullName;

    /** Номер телефона */
    @Size(max = 20)
    private String phone;

    /** Электронная почта */
    @Email(message = "Некорректный формат email")
    @Size(max = 100)
    private String email;

    /** Роль пользователя в системе */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /** Дата и время регистрации */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Флаг активности учётной записи */
    @Column(nullable = false)
    private boolean enabled = true;

    /** Список заказов клиента */
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();

    /**
     * Конструктор по умолчанию.
     * Устанавливает дату регистрации.
     */
    public User() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Конструктор с основными параметрами.
     *
     * @param username логин
     * @param password хешированный пароль
     * @param fullName полное имя
     * @param role     роль пользователя
     */
    public User(String username, String password, String fullName, Role role) {
        this();
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}

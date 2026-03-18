package com.taxiservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

/**
 * Сущность транспортного средства.
 * Содержит информацию об автомобиле, закреплённом за водителем.
 * Связь: многие транспортные средства принадлежат одному водителю (родитель-дочка).
 *
 * @version 1.0
 */
@Entity
@Table(name = "vehicles")
public class Vehicle {

    /** Уникальный идентификатор транспортного средства */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Водитель, за которым закреплено транспортное средство */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    /** Марка автомобиля */
    @NotBlank(message = "Марка не может быть пустой")
    @Column(nullable = false)
    private String brand;

    /** Модель автомобиля */
    @NotBlank(message = "Модель не может быть пустой")
    @Column(nullable = false)
    private String model;

    /** Цвет автомобиля */
    @NotBlank(message = "Цвет не может быть пустым")
    @Column(nullable = false)
    private String color;

    /** Государственный регистрационный номер */
    @NotBlank(message = "Госномер не может быть пустым")
    @Column(name = "license_plate", nullable = false, unique = true)
    private String licensePlate;

    /** Год выпуска */
    @Min(value = 2000, message = "Год выпуска должен быть не ранее 2000")
    @Column(name = "manufacture_year", nullable = false)
    private int year;

    public Vehicle() {
    }

    /**
     * Конструктор с основными параметрами.
     *
     * @param driver       водитель
     * @param brand        марка
     * @param model        модель
     * @param color        цвет
     * @param licensePlate госномер
     * @param year         год выпуска
     */
    public Vehicle(Driver driver, String brand, String model, String color,
                   String licensePlate, int year) {
        this.driver = driver;
        this.brand = brand;
        this.model = model;
        this.color = color;
        this.licensePlate = licensePlate;
        this.year = year;
    }

    /**
     * Возвращает полное описание автомобиля.
     *
     * @return строка формата "Марка Модель (Цвет, Год)"
     */
    public String getFullDescription() {
        return brand + " " + model + " (" + color + ", " + year + ")";
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}

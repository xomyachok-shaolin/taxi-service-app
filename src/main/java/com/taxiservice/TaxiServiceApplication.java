package com.taxiservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс приложения "Информационно-справочная система службы такси".
 * Запускает Spring Boot приложение с встроенным сервером Tomcat.
 *
 * @version 1.0
 */
@SpringBootApplication
public class TaxiServiceApplication {

    /**
     * Точка входа в приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(TaxiServiceApplication.class, args);
    }
}

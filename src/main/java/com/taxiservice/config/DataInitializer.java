package com.taxiservice.config;

import com.taxiservice.model.*;
import com.taxiservice.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Инициализатор тестовых данных.
 * Заполняет базу данных начальными записями при первом запуске приложения:
 * пользователи, водители, транспортные средства и заказы.
 *
 * @version 1.0
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           DriverRepository driverRepository,
                           VehicleRepository vehicleRepository,
                           OrderRepository orderRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
        this.orderRepository = orderRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Выполняется при запуске приложения.
     * Создаёт тестовые данные, если база данных пуста.
     *
     * @param args аргументы командной строки
     */
    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // Данные уже существуют
        }

        // === Администратор ===
        User admin = new User("admin", passwordEncoder.encode("admin123"),
                "Иванов Иван Иванович", Role.ADMIN);
        admin.setPhone("+7 (900) 111-11-11");
        admin.setEmail("admin@taxi-service.ru");
        admin = userRepository.save(admin);

        // === Диспетчеры ===
        User dispatcher1 = new User("dispatcher", passwordEncoder.encode("disp123"),
                "Петрова Анна Сергеевна", Role.DISPATCHER);
        dispatcher1.setPhone("+7 (900) 222-22-22");
        dispatcher1.setEmail("dispatcher@taxi-service.ru");
        dispatcher1 = userRepository.save(dispatcher1);

        User dispatcher2 = new User("dispatcher2", passwordEncoder.encode("disp123"),
                "Козлова Мария Дмитриевна", Role.DISPATCHER);
        dispatcher2.setPhone("+7 (900) 222-33-33");
        dispatcher2.setEmail("dispatcher2@taxi-service.ru");
        dispatcher2 = userRepository.save(dispatcher2);

        // === Водители ===
        User driverUser1 = new User("driver1", passwordEncoder.encode("driver123"),
                "Сидоров Алексей Петрович", Role.DRIVER);
        driverUser1.setPhone("+7 (900) 333-33-33");
        driverUser1.setEmail("driver1@taxi-service.ru");
        driverUser1 = userRepository.save(driverUser1);

        Driver driver1 = new Driver(driverUser1, "77 АА 123456");
        driver1.setRating(4.8);
        driver1.setStatus("AVAILABLE");
        driver1 = driverRepository.save(driver1);

        Vehicle vehicle1 = new Vehicle(driver1, "Toyota", "Camry",
                "Белый", "А123ВС77", 2022);
        vehicleRepository.save(vehicle1);

        User driverUser2 = new User("driver2", passwordEncoder.encode("driver123"),
                "Кузнецов Дмитрий Александрович", Role.DRIVER);
        driverUser2.setPhone("+7 (900) 444-44-44");
        driverUser2.setEmail("driver2@taxi-service.ru");
        driverUser2 = userRepository.save(driverUser2);

        Driver driver2 = new Driver(driverUser2, "77 ВВ 654321");
        driver2.setRating(4.5);
        driver2.setStatus("AVAILABLE");
        driver2 = driverRepository.save(driver2);

        Vehicle vehicle2 = new Vehicle(driver2, "Hyundai", "Solaris",
                "Серый", "В456ОР77", 2021);
        vehicleRepository.save(vehicle2);

        User driverUser3 = new User("driver3", passwordEncoder.encode("driver123"),
                "Новиков Сергей Владимирович", Role.DRIVER);
        driverUser3.setPhone("+7 (900) 555-55-55");
        driverUser3.setEmail("driver3@taxi-service.ru");
        driverUser3 = userRepository.save(driverUser3);

        Driver driver3 = new Driver(driverUser3, "77 СС 789012");
        driver3.setRating(4.9);
        driver3.setStatus("BUSY");
        driver3 = driverRepository.save(driver3);

        Vehicle vehicle3 = new Vehicle(driver3, "Kia", "Rio",
                "Чёрный", "С789КМ77", 2023);
        vehicleRepository.save(vehicle3);

        Vehicle vehicle3b = new Vehicle(driver3, "Volkswagen", "Polo",
                "Синий", "Е012НС77", 2020);
        vehicleRepository.save(vehicle3b);

        // === Клиенты ===
        User client1 = new User("client1", passwordEncoder.encode("client123"),
                "Морозова Елена Викторовна", Role.CLIENT);
        client1.setPhone("+7 (900) 666-66-66");
        client1.setEmail("morozova@mail.ru");
        client1 = userRepository.save(client1);

        User client2 = new User("client2", passwordEncoder.encode("client123"),
                "Волков Андрей Николаевич", Role.CLIENT);
        client2.setPhone("+7 (900) 777-77-77");
        client2.setEmail("volkov@mail.ru");
        client2 = userRepository.save(client2);

        User client3 = new User("client3", passwordEncoder.encode("client123"),
                "Лебедева Ольга Андреевна", Role.CLIENT);
        client3.setPhone("+7 (900) 888-88-88");
        client3.setEmail("lebedeva@mail.ru");
        client3 = userRepository.save(client3);

        // === Заказы ===
        // Завершённые заказы
        Order order1 = new Order(client1, "ул. Тверская, 10", "Шереметьево, Терминал D");
        order1.setDriver(driver1);
        order1.setStatus(OrderStatus.COMPLETED);
        order1.setOrderTime(LocalDateTime.now().minusDays(2).minusHours(3));
        order1.setPickupTime(LocalDateTime.now().minusDays(2).minusHours(3).plusMinutes(8));
        order1.setCompletionTime(LocalDateTime.now().minusDays(2).minusHours(2));
        order1.setDistance(35.2);
        order1.setPrice(new BigDecimal("1030.00"));
        orderRepository.save(order1);

        Order order2 = new Order(client2, "Красная площадь, 1", "ул. Арбат, 25");
        order2.setDriver(driver2);
        order2.setStatus(OrderStatus.COMPLETED);
        order2.setOrderTime(LocalDateTime.now().minusDays(1).minusHours(5));
        order2.setPickupTime(LocalDateTime.now().minusDays(1).minusHours(5).plusMinutes(12));
        order2.setCompletionTime(LocalDateTime.now().minusDays(1).minusHours(4).minusMinutes(30));
        order2.setDistance(5.8);
        order2.setPrice(new BigDecimal("295.00"));
        orderRepository.save(order2);

        Order order3 = new Order(client1, "ул. Ленина, 45", "Курский вокзал");
        order3.setDriver(driver3);
        order3.setStatus(OrderStatus.COMPLETED);
        order3.setOrderTime(LocalDateTime.now().minusDays(1).minusHours(2));
        order3.setPickupTime(LocalDateTime.now().minusDays(1).minusHours(2).plusMinutes(5));
        order3.setCompletionTime(LocalDateTime.now().minusDays(1).minusHours(1).minusMinutes(20));
        order3.setDistance(12.3);
        order3.setPrice(new BigDecimal("457.50"));
        orderRepository.save(order3);

        Order order4 = new Order(client3, "пр. Мира, 78", "ул. Бауманская, 15");
        order4.setDriver(driver1);
        order4.setStatus(OrderStatus.COMPLETED);
        order4.setOrderTime(LocalDateTime.now().minusHours(10));
        order4.setPickupTime(LocalDateTime.now().minusHours(10).plusMinutes(7));
        order4.setCompletionTime(LocalDateTime.now().minusHours(9).minusMinutes(30));
        order4.setDistance(8.7);
        order4.setPrice(new BigDecimal("367.50"));
        orderRepository.save(order4);

        // Активные заказы
        Order order5 = new Order(client2, "ул. Пушкина, 30", "Домодедово, Терминал 1");
        order5.setDriver(driver3);
        order5.setStatus(OrderStatus.IN_PROGRESS);
        order5.setOrderTime(LocalDateTime.now().minusMinutes(25));
        order5.setPickupTime(LocalDateTime.now().minusMinutes(15));
        order5.setDistance(42.1);
        order5.setPrice(new BigDecimal("1202.50"));
        orderRepository.save(order5);

        Order order6 = new Order(client3, "ул. Гагарина, 5", "ТЦ Авиапарк");
        order6.setStatus(OrderStatus.CREATED);
        order6.setOrderTime(LocalDateTime.now().minusMinutes(3));
        order6.setDistance(7.5);
        order6.setPrice(new BigDecimal("337.50"));
        orderRepository.save(order6);

        // Заказы за предыдущие дни (для статистики)
        for (int i = 2; i <= 6; i++) {
            Order histOrder = new Order(client1, "Адрес подачи " + i, "Адрес назначения " + i);
            histOrder.setDriver(i % 2 == 0 ? driver1 : driver2);
            histOrder.setStatus(OrderStatus.COMPLETED);
            histOrder.setOrderTime(LocalDateTime.now().minusDays(i).minusHours(3));
            histOrder.setPickupTime(LocalDateTime.now().minusDays(i).minusHours(3).plusMinutes(6 + i));
            histOrder.setCompletionTime(LocalDateTime.now().minusDays(i).minusHours(2));
            histOrder.setDistance(5.0 + i * 2.5);
            histOrder.setPrice(BigDecimal.valueOf(200 + i * 50));
            orderRepository.save(histOrder);
        }

        System.out.println("=== Тестовые данные загружены ===");
        System.out.println("Администратор: admin / admin123");
        System.out.println("Диспетчер: dispatcher / disp123");
        System.out.println("Водитель: driver1 / driver123");
        System.out.println("Клиент: client1 / client123");
    }
}

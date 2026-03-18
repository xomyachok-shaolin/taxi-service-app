package com.taxiservice.controller;

import com.taxiservice.model.Driver;
import com.taxiservice.model.Order;
import com.taxiservice.model.OrderStatus;
import com.taxiservice.model.User;
import com.taxiservice.model.Role;
import com.taxiservice.service.DriverService;
import com.taxiservice.service.OrderService;
import com.taxiservice.service.StatisticsService;
import com.taxiservice.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для диспетчерской части приложения.
 * Обрабатывает операции диспетчера: просмотр всех заказов, назначение водителей,
 * просмотр водителей и клиентов, отображение статистики.
 *
 * @version 1.0
 */
@Controller
@RequestMapping("/dispatcher")
public class DispatcherController {

    private final UserService userService;
    private final OrderService orderService;
    private final DriverService driverService;
    private final StatisticsService statisticsService;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param userService       сервис управления пользователями
     * @param orderService      сервис управления заказами
     * @param driverService     сервис управления водителями
     * @param statisticsService сервис статистики
     */
    public DispatcherController(UserService userService, OrderService orderService,
                                DriverService driverService, StatisticsService statisticsService) {
        this.userService = userService;
        this.orderService = orderService;
        this.driverService = driverService;
        this.statisticsService = statisticsService;
    }

    /**
     * Отображение списка всех заказов с возможностью поиска, сортировки и фильтрации по статусу.
     *
     * @param search    строка поиска по адресу (необязательно)
     * @param status    фильтр по статусу заказа (необязательно)
     * @param sort      поле сортировки: date, price, distance, status (необязательно)
     * @param direction направление сортировки: asc или desc (по умолчанию desc)
     * @param model     модель для передачи данных в шаблон
     * @param principal текущий аутентифицированный пользователь
     * @return имя шаблона списка заказов диспетчера
     */
    @GetMapping("/orders")
    public String listOrders(@RequestParam(required = false) String search,
                             @RequestParam(required = false) String status,
                             @RequestParam(required = false, defaultValue = "date") String sort,
                             @RequestParam(required = false, defaultValue = "desc") String direction,
                             Model model, Principal principal) {
        try {
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            List<Order> orders;

            if (search != null && !search.trim().isEmpty()) {
                orders = orderService.searchByAddress(search.trim());
                model.addAttribute("search", search);
            } else if (status != null && !status.trim().isEmpty()) {
                try {
                    OrderStatus orderStatus = OrderStatus.valueOf(status);
                    orders = orderService.findByStatus(orderStatus);
                } catch (IllegalArgumentException e) {
                    orders = orderService.findAll();
                }
                model.addAttribute("selectedStatus", status);
            } else {
                orders = orderService.findAll();
            }

            boolean ascending = "asc".equalsIgnoreCase(direction);
            orders = orderService.sort(orders, sort, ascending);

            // Получаем список доступных водителей для назначения
            List<Driver> availableDrivers = driverService.findAvailable();

            model.addAttribute("orders", orders);
            model.addAttribute("availableDrivers", availableDrivers);
            model.addAttribute("statuses", OrderStatus.values());
            model.addAttribute("currentSort", sort);
            model.addAttribute("currentDirection", direction);
            model.addAttribute("user", user);

            return "dispatcher/orders";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка загрузки заказов: " + e.getMessage());
            return "dispatcher/orders";
        }
    }

    /**
     * Просмотр подробной информации о заказе.
     *
     * @param id        идентификатор заказа
     * @param model     модель для передачи данных в шаблон
     * @param principal текущий аутентифицированный пользователь
     * @return имя шаблона деталей заказа
     */
    @GetMapping("/orders/{id}")
    public String viewOrder(@PathVariable Long id, Model model, Principal principal) {
        try {
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            Order order = orderService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Заказ не найден"));

            List<Driver> availableDrivers = driverService.findAvailable();

            model.addAttribute("order", order);
            model.addAttribute("availableDrivers", availableDrivers);
            model.addAttribute("user", user);
            return "dispatcher/orders";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Назначение водителя на заказ.
     *
     * @param id                 идентификатор заказа
     * @param driverId           идентификатор назначаемого водителя
     * @param redirectAttributes атрибуты для передачи flash-сообщений
     * @return перенаправление на список заказов диспетчера
     */
    @PostMapping("/orders/{id}/assign")
    public String assignDriver(@PathVariable Long id,
                               @RequestParam Long driverId,
                               RedirectAttributes redirectAttributes) {
        try {
            Driver driver = driverService.findById(driverId)
                    .orElseThrow(() -> new RuntimeException("Водитель не найден"));

            orderService.assignDriver(id, driver);

            // Устанавливаем водителю статус BUSY
            driverService.updateStatus(driverId, "BUSY");

            redirectAttributes.addFlashAttribute("success",
                    "Водитель " + driver.getUser().getFullName() + " назначен на заказ #" + id);
            return "redirect:/dispatcher/orders";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка назначения водителя: " + e.getMessage());
            return "redirect:/dispatcher/orders";
        }
    }

    /**
     * Отмена заказа диспетчером.
     *
     * @param id                 идентификатор заказа
     * @param redirectAttributes атрибуты для передачи flash-сообщений
     * @return перенаправление на список заказов
     */
    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(@PathVariable Long id,
                              RedirectAttributes redirectAttributes) {
        try {
            orderService.updateStatus(id, OrderStatus.CANCELLED);
            redirectAttributes.addFlashAttribute("success", "Заказ #" + id + " отменён");
            return "redirect:/dispatcher/orders";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка отмены заказа: " + e.getMessage());
            return "redirect:/dispatcher/orders";
        }
    }

    /**
     * Удаление заказа диспетчером.
     *
     * @param id                 идентификатор заказа
     * @param redirectAttributes атрибуты для передачи flash-сообщений
     * @return перенаправление на список заказов
     */
    @PostMapping("/orders/{id}/delete")
    public String deleteOrder(@PathVariable Long id,
                              RedirectAttributes redirectAttributes) {
        try {
            orderService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Заказ #" + id + " удалён");
            return "redirect:/dispatcher/orders";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка удаления заказа: " + e.getMessage());
            return "redirect:/dispatcher/orders";
        }
    }

    /**
     * Отображение списка всех водителей с их статусами.
     *
     * @param model     модель для передачи данных в шаблон
     * @param principal текущий аутентифицированный пользователь
     * @return имя шаблона списка водителей
     */
    @GetMapping("/drivers")
    public String listDrivers(Model model, Principal principal) {
        try {
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            List<Driver> drivers = driverService.findAll();

            model.addAttribute("drivers", drivers);
            model.addAttribute("user", user);
            return "dispatcher/drivers";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка загрузки водителей: " + e.getMessage());
            return "dispatcher/drivers";
        }
    }

    /**
     * Отображение списка всех клиентов.
     *
     * @param model     модель для передачи данных в шаблон
     * @param principal текущий аутентифицированный пользователь
     * @return имя шаблона списка клиентов
     */
    @GetMapping("/clients")
    public String listClients(Model model, Principal principal) {
        try {
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            List<User> clients = userService.findByRole(Role.CLIENT);

            model.addAttribute("clients", clients);
            model.addAttribute("user", user);
            return "dispatcher/clients";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка загрузки клиентов: " + e.getMessage());
            return "dispatcher/clients";
        }
    }

    /**
     * Отображение страницы статистики для диспетчера.
     * Включает количество заказов, водителей, среднее время ожидания,
     * распределение по статусам и заказы за последние 7 дней.
     *
     * @param model     модель для передачи данных в шаблон
     * @param principal текущий аутентифицированный пользователь
     * @return имя шаблона страницы статистики
     */
    @GetMapping("/statistics")
    public String statistics(Model model, Principal principal) {
        try {
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            model.addAttribute("totalUsers", statisticsService.getTotalUsers());
            model.addAttribute("totalOrders", statisticsService.getTotalOrders());
            model.addAttribute("completedOrders", statisticsService.getCompletedOrders());
            model.addAttribute("activeOrders", statisticsService.getActiveOrders());
            model.addAttribute("totalDrivers", statisticsService.getTotalDrivers());
            model.addAttribute("availableDrivers", statisticsService.getAvailableDrivers());
            model.addAttribute("totalClients", statisticsService.getTotalClients());
            model.addAttribute("averageWaitingTime", statisticsService.getAverageWaitingTime());
            model.addAttribute("averagePrice", statisticsService.getAveragePrice());
            model.addAttribute("averageDistance", statisticsService.getAverageDistance());
            model.addAttribute("avgWaitTime", statisticsService.getAverageWaitingTime());
            model.addAttribute("avgPrice", statisticsService.getAveragePrice());

            Map<String, Long> ordersByStatus = statisticsService.getOrdersByStatus();
            model.addAttribute("ordersByStatus", ordersByStatus);
            model.addAttribute("statusLabels", new ArrayList<>(ordersByStatus.keySet()));
            model.addAttribute("statusCounts", new ArrayList<>(ordersByStatus.values()));

            Map<String, Long> ordersPerDay = statisticsService.getOrdersPerDay();
            model.addAttribute("ordersPerDay", ordersPerDay);
            model.addAttribute("dayLabels", new ArrayList<>(ordersPerDay.keySet()));
            model.addAttribute("dayCounts", new ArrayList<>(ordersPerDay.values()));

            Map<String, Long> usersByRole = statisticsService.getUsersByRole();
            model.addAttribute("usersByRole", usersByRole);
            model.addAttribute("roleLabels", new ArrayList<>(usersByRole.keySet()));
            model.addAttribute("roleCounts", new ArrayList<>(usersByRole.values()));

            model.addAttribute("user", user);

            return "dispatcher/statistics";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка загрузки статистики: " + e.getMessage());
            return "dispatcher/statistics";
        }
    }
}

package com.taxiservice.controller;

import com.taxiservice.model.Driver;
import com.taxiservice.model.Order;
import com.taxiservice.model.OrderStatus;
import com.taxiservice.model.User;
import com.taxiservice.model.Vehicle;
import com.taxiservice.service.DriverService;
import com.taxiservice.service.OrderService;
import com.taxiservice.service.UserService;
import com.taxiservice.service.VehicleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

/**
 * Контроллер для водительской части приложения.
 * Обрабатывает операции водителя: просмотр назначенных заказов,
 * обновление статуса заказа, управление профилем и доступностью,
 * просмотр транспортных средств.
 *
 * @version 1.0
 */
@Controller
@RequestMapping("/driver")
public class DriverController {

    private final UserService userService;
    private final DriverService driverService;
    private final OrderService orderService;
    private final VehicleService vehicleService;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param userService    сервис управления пользователями
     * @param driverService  сервис управления водителями
     * @param orderService   сервис управления заказами
     * @param vehicleService сервис управления транспортными средствами
     */
    public DriverController(UserService userService, DriverService driverService,
                            OrderService orderService, VehicleService vehicleService) {
        this.userService = userService;
        this.driverService = driverService;
        this.orderService = orderService;
        this.vehicleService = vehicleService;
    }

    /**
     * Отображение списка заказов, назначенных водителю, с возможностью поиска и сортировки.
     *
     * @param search    строка поиска по адресу (необязательно)
     * @param sort      поле сортировки: date, price, distance, status (необязательно)
     * @param direction направление сортировки: asc или desc (по умолчанию desc)
     * @param model     модель для передачи данных в шаблон
     * @param principal текущий аутентифицированный пользователь
     * @return имя шаблона списка заказов водителя
     */
    @GetMapping("/orders")
    public String listOrders(@RequestParam(required = false) String search,
                             @RequestParam(required = false, defaultValue = "date") String sort,
                             @RequestParam(required = false, defaultValue = "desc") String direction,
                             Model model, Principal principal) {
        try {
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            Driver driver = driverService.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Профиль водителя не найден"));

            List<Order> orders;
            if (search != null && !search.trim().isEmpty()) {
                orders = orderService.searchByAddress(search.trim());
                // Фильтруем только заказы текущего водителя
                orders.removeIf(order -> order.getDriver() == null
                        || !order.getDriver().getId().equals(driver.getId()));
                model.addAttribute("search", search);
            } else {
                orders = orderService.findByDriverId(driver.getId());
            }

            boolean ascending = "asc".equalsIgnoreCase(direction);
            orders = orderService.sort(orders, sort, ascending);

            model.addAttribute("orders", orders);
            model.addAttribute("currentSort", sort);
            model.addAttribute("currentDirection", direction);
            model.addAttribute("user", user);
            model.addAttribute("driver", driver);

            return "driver/orders";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка загрузки заказов: " + e.getMessage());
            return "driver/orders";
        }
    }

    /**
     * Обновление статуса заказа водителем.
     * Поддерживаемые действия:
     * <ul>
     *     <li>accept — принять заказ (ASSIGNED -> EN_ROUTE)</li>
     *     <li>start — начать поездку (EN_ROUTE -> IN_PROGRESS)</li>
     *     <li>complete — завершить поездку (IN_PROGRESS -> COMPLETED)</li>
     * </ul>
     *
     * @param id                 идентификатор заказа
     * @param action             действие: accept, start, complete
     * @param principal          текущий аутентифицированный пользователь
     * @param redirectAttributes атрибуты для передачи flash-сообщений
     * @return перенаправление на список заказов водителя
     */
    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam String action,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            Driver driver = driverService.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Профиль водителя не найден"));

            Order order = orderService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Заказ не найден"));

            // Проверяем, что заказ назначен текущему водителю
            if (order.getDriver() == null || !order.getDriver().getId().equals(driver.getId())) {
                redirectAttributes.addFlashAttribute("error", "Этот заказ не назначен вам");
                return "redirect:/driver/orders";
            }

            OrderStatus newStatus;
            String message;

            switch (action) {
                case "accept":
                    newStatus = OrderStatus.EN_ROUTE;
                    message = "Заказ принят. Вы в пути к клиенту.";
                    break;
                case "start":
                    newStatus = OrderStatus.IN_PROGRESS;
                    message = "Поездка начата.";
                    break;
                case "complete":
                    newStatus = OrderStatus.COMPLETED;
                    message = "Поездка завершена.";
                    // Переключаем водителя обратно в AVAILABLE после завершения
                    driverService.updateStatus(driver.getId(), "AVAILABLE");
                    break;
                default:
                    redirectAttributes.addFlashAttribute("error", "Неизвестное действие: " + action);
                    return "redirect:/driver/orders";
            }

            orderService.updateStatus(id, newStatus);
            redirectAttributes.addFlashAttribute("success", message);
            return "redirect:/driver/orders";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка обновления статуса: " + e.getMessage());
            return "redirect:/driver/orders";
        }
    }

    /**
     * Отображение профиля водителя.
     *
     * @param model     модель для передачи данных в шаблон
     * @param principal текущий аутентифицированный пользователь
     * @return имя шаблона профиля водителя
     */
    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        try {
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            Driver driver = driverService.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Профиль водителя не найден"));

            List<Vehicle> vehicles = vehicleService.findByDriverId(driver.getId());

            model.addAttribute("user", user);
            model.addAttribute("driver", driver);
            model.addAttribute("vehicles", vehicles);
            return "driver/profile";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка загрузки профиля: " + e.getMessage());
            return "driver/profile";
        }
    }

    /**
     * Переключение статуса доступности водителя (AVAILABLE / OFFLINE).
     *
     * @param status             новый статус водителя
     * @param principal          текущий аутентифицированный пользователь
     * @param redirectAttributes атрибуты для передачи flash-сообщений
     * @return перенаправление на список заказов водителя
     */
    @PostMapping("/status")
    public String toggleStatus(@RequestParam String status,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            Driver driver = driverService.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Профиль водителя не найден"));

            // Разрешаем только AVAILABLE и OFFLINE
            if (!"AVAILABLE".equals(status) && !"OFFLINE".equals(status)) {
                redirectAttributes.addFlashAttribute("error", "Недопустимый статус: " + status);
                return "redirect:/driver/orders";
            }

            driverService.updateStatus(driver.getId(), status);
            String statusText = "AVAILABLE".equals(status) ? "Доступен" : "Не в сети";
            redirectAttributes.addFlashAttribute("success",
                    "Статус изменён: " + statusText);
            return "redirect:/driver/orders";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка изменения статуса: " + e.getMessage());
            return "redirect:/driver/orders";
        }
    }

    /**
     * Отображение списка транспортных средств водителя.
     *
     * @param model     модель для передачи данных в шаблон
     * @param principal текущий аутентифицированный пользователь
     * @return имя шаблона списка транспортных средств
     */
    @GetMapping("/vehicles")
    public String listVehicles(Model model, Principal principal) {
        try {
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            Driver driver = driverService.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Профиль водителя не найден"));

            List<Vehicle> vehicles = vehicleService.findByDriverId(driver.getId());

            model.addAttribute("vehicles", vehicles);
            model.addAttribute("user", user);
            model.addAttribute("driver", driver);
            return "driver/profile";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка загрузки транспортных средств: " + e.getMessage());
            return "driver/profile";
        }
    }
}

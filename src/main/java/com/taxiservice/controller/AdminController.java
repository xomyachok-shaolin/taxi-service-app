package com.taxiservice.controller;

import com.taxiservice.model.Driver;
import com.taxiservice.model.Role;
import com.taxiservice.model.User;
import com.taxiservice.service.DriverService;
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
 * Контроллер для административной части приложения.
 * Обрабатывает операции администратора: управление пользователями (просмотр, изменение ролей,
 * включение/отключение, удаление), управление водителями, просмотр статистики.
 *
 * @version 1.0
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final DriverService driverService;
    private final StatisticsService statisticsService;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param userService       сервис управления пользователями
     * @param driverService     сервис управления водителями
     * @param statisticsService сервис статистики
     */
    public AdminController(UserService userService, DriverService driverService,
                           StatisticsService statisticsService) {
        this.userService = userService;
        this.driverService = driverService;
        this.statisticsService = statisticsService;
    }

    /**
     * Отображение списка всех пользователей с возможностью поиска.
     *
     * @param search    строка поиска по имени (необязательно)
     * @param model     модель для передачи данных в шаблон
     * @param principal текущий аутентифицированный пользователь
     * @return имя шаблона списка пользователей
     */
    @GetMapping("/users")
    public String listUsers(@RequestParam(required = false) String search,
                            Model model, Principal principal) {
        try {
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            List<User> users;
            if (search != null && !search.trim().isEmpty()) {
                users = userService.searchByName(search.trim());
                model.addAttribute("search", search);
            } else {
                users = userService.findAll();
            }

            model.addAttribute("users", users);
            model.addAttribute("roles", Role.values());
            model.addAttribute("currentUser", user);

            return "admin/users";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка загрузки пользователей: " + e.getMessage());
            return "admin/users";
        }
    }

    /**
     * Изменение роли пользователя.
     *
     * @param id                 идентификатор пользователя
     * @param role               новая роль
     * @param principal          текущий аутентифицированный пользователь
     * @param redirectAttributes атрибуты для передачи flash-сообщений
     * @return перенаправление на список пользователей
     */
    @PostMapping("/users/{id}/role")
    public String changeRole(@PathVariable Long id,
                             @RequestParam String role,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            // Запрещаем менять свою собственную роль
            if (currentUser.getId().equals(id)) {
                redirectAttributes.addFlashAttribute("error",
                        "Нельзя изменить свою собственную роль");
                return "redirect:/admin/users";
            }

            Role newRole = Role.valueOf(role);
            User updatedUser = userService.changeRole(id, newRole);

            // Если пользователю назначена роль DRIVER, создаём профиль водителя (если нет)
            if (newRole == Role.DRIVER && driverService.findByUserId(id).isEmpty()) {
                Driver driver = new Driver(updatedUser, "Не указан");
                driverService.save(driver);
            }

            redirectAttributes.addFlashAttribute("success",
                    "Роль пользователя " + updatedUser.getFullName() + " изменена на " + newRole);
            return "redirect:/admin/users";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка изменения роли: " + e.getMessage());
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }

    /**
     * Включение или отключение учётной записи пользователя.
     *
     * @param id                 идентификатор пользователя
     * @param principal          текущий аутентифицированный пользователь
     * @param redirectAttributes атрибуты для передачи flash-сообщений
     * @return перенаправление на список пользователей
     */
    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            // Запрещаем блокировать самого себя
            if (currentUser.getId().equals(id)) {
                redirectAttributes.addFlashAttribute("error",
                        "Нельзя заблокировать свою собственную учётную запись");
                return "redirect:/admin/users";
            }

            User targetUser = userService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            targetUser.setEnabled(!targetUser.isEnabled());
            userService.update(targetUser);

            String statusText = targetUser.isEnabled() ? "активирована" : "заблокирована";
            redirectAttributes.addFlashAttribute("success",
                    "Учётная запись " + targetUser.getFullName() + " " + statusText);
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }

    /**
     * Удаление пользователя из системы.
     *
     * @param id                 идентификатор пользователя
     * @param principal          текущий аутентифицированный пользователь
     * @param redirectAttributes атрибуты для передачи flash-сообщений
     * @return перенаправление на список пользователей
     */
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            // Запрещаем удалять самого себя
            if (currentUser.getId().equals(id)) {
                redirectAttributes.addFlashAttribute("error",
                        "Нельзя удалить свою собственную учётную запись");
                return "redirect:/admin/users";
            }

            User targetUser = userService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            // Удаляем профиль водителя, если есть
            driverService.findByUserId(id).ifPresent(driver ->
                    driverService.delete(driver.getId()));

            userService.delete(id);

            redirectAttributes.addFlashAttribute("success",
                    "Пользователь " + targetUser.getFullName() + " удалён");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка удаления: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }

    /**
     * Отображение страницы статистики для администратора.
     * Включает те же данные, что и статистика диспетчера,
     * а также общую информацию о пользователях.
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

            return "admin/statistics";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка загрузки статистики: " + e.getMessage());
            return "admin/statistics";
        }
    }

    /**
     * Отображение списка водителей для управления.
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
            return "admin/users";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка загрузки водителей: " + e.getMessage());
            return "admin/users";
        }
    }
}

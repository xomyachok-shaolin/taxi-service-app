package com.taxiservice.controller;

import com.taxiservice.model.Order;
import com.taxiservice.model.OrderStatus;
import com.taxiservice.model.User;
import com.taxiservice.service.OrderService;
import com.taxiservice.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

/**
 * Контроллер для клиентской части приложения.
 * Обрабатывает операции клиента: просмотр заказов, создание нового заказа,
 * отмена заказа, просмотр и редактирование профиля.
 *
 * @version 1.0
 */
@Controller
@RequestMapping("/client")
public class ClientController {

    private final UserService userService;
    private final OrderService orderService;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param userService  сервис управления пользователями
     * @param orderService сервис управления заказами
     */
    public ClientController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }

    /**
     * Отображение списка заказов клиента с возможностью поиска и сортировки.
     *
     * @param search    строка поиска по адресу (необязательно)
     * @param sort      поле сортировки: date, price, distance, status (необязательно)
     * @param direction направление сортировки: asc или desc (по умолчанию desc)
     * @param model     модель для передачи данных в шаблон
     * @param principal текущий аутентифицированный пользователь
     * @return имя шаблона списка заказов клиента
     */
    @GetMapping("/orders")
    public String listOrders(@RequestParam(required = false) String search,
                             @RequestParam(required = false, defaultValue = "date") String sort,
                             @RequestParam(required = false, defaultValue = "desc") String direction,
                             Model model, Principal principal) {
        try {
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            List<Order> orders;
            if (search != null && !search.trim().isEmpty()) {
                orders = orderService.searchByAddress(search.trim());
                // Фильтруем только заказы текущего клиента
                orders.removeIf(order -> !order.getClient().getId().equals(user.getId()));
                model.addAttribute("search", search);
            } else {
                orders = orderService.findByClientId(user.getId());
            }

            boolean ascending = "asc".equalsIgnoreCase(direction);
            orders = orderService.sort(orders, sort, ascending);

            model.addAttribute("orders", orders);
            model.addAttribute("currentSort", sort);
            model.addAttribute("currentDirection", direction);
            model.addAttribute("user", user);

            return "client/orders";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка загрузки заказов: " + e.getMessage());
            return "client/orders";
        }
    }

    /**
     * Отображение формы создания нового заказа.
     *
     * @param model     модель для передачи данных в шаблон
     * @param principal текущий аутентифицированный пользователь
     * @return имя шаблона формы нового заказа
     */
    @GetMapping("/orders/new")
    public String newOrderForm(Model model, Principal principal) {
        try {
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            model.addAttribute("order", new Order());
            model.addAttribute("user", user);
            return "client/order-new";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка: " + e.getMessage());
            return "client/order-new";
        }
    }

    /**
     * Создание нового заказа такси.
     *
     * @param order              данные заказа из формы
     * @param principal          текущий аутентифицированный пользователь
     * @param redirectAttributes атрибуты для передачи flash-сообщений
     * @return перенаправление на список заказов
     */
    @PostMapping("/orders")
    public String createOrder(@ModelAttribute Order order, Principal principal,
                              RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            order.setClient(user);
            orderService.createOrder(order);

            redirectAttributes.addFlashAttribute("success", "Заказ успешно создан!");
            return "redirect:/client/orders";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка при создании заказа: " + e.getMessage());
            return "redirect:/client/orders/new";
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

            // Проверяем, что заказ принадлежит текущему клиенту
            if (!order.getClient().getId().equals(user.getId())) {
                model.addAttribute("error", "Доступ запрещён");
                return "error";
            }

            model.addAttribute("order", order);
            model.addAttribute("user", user);
            return "client/order-detail";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Отмена заказа клиентом.
     * Заказ можно отменить только если он ещё не начат (статус CREATED или ASSIGNED).
     *
     * @param id                 идентификатор заказа
     * @param principal          текущий аутентифицированный пользователь
     * @param redirectAttributes атрибуты для передачи flash-сообщений
     * @return перенаправление на список заказов
     */
    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(@PathVariable Long id, Principal principal,
                              RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            Order order = orderService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Заказ не найден"));

            // Проверяем, что заказ принадлежит текущему клиенту
            if (!order.getClient().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "Доступ запрещён");
                return "redirect:/client/orders";
            }

            // Проверяем возможность отмены
            if (order.getStatus() == OrderStatus.COMPLETED
                    || order.getStatus() == OrderStatus.CANCELLED) {
                redirectAttributes.addFlashAttribute("error",
                        "Невозможно отменить заказ со статусом: " + order.getStatus());
                return "redirect:/client/orders";
            }

            orderService.updateStatus(id, OrderStatus.CANCELLED);
            redirectAttributes.addFlashAttribute("success", "Заказ успешно отменён.");
            return "redirect:/client/orders";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка при отмене заказа: " + e.getMessage());
            return "redirect:/client/orders";
        }
    }

    /**
     * Отображение профиля клиента с возможностью редактирования.
     *
     * @param model     модель для передачи данных в шаблон
     * @param principal текущий аутентифицированный пользователь
     * @return имя шаблона профиля клиента
     */
    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        try {
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            model.addAttribute("user", user);
            return "client/profile";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка загрузки профиля: " + e.getMessage());
            return "client/profile";
        }
    }

    /**
     * Обновление профиля клиента.
     *
     * @param fullName           новое ФИО
     * @param phone              новый номер телефона
     * @param email              новый адрес электронной почты
     * @param principal          текущий аутентифицированный пользователь
     * @param redirectAttributes атрибуты для передачи flash-сообщений
     * @return перенаправление на страницу профиля
     */
    @PostMapping("/profile")
    public String updateProfile(@RequestParam String fullName,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String email,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            user.setFullName(fullName);
            user.setPhone(phone);
            user.setEmail(email);
            userService.update(user);

            redirectAttributes.addFlashAttribute("success", "Профиль успешно обновлён.");
            return "redirect:/client/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка обновления профиля: " + e.getMessage());
            return "redirect:/client/profile";
        }
    }
}

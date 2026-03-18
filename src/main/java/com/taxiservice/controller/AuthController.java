package com.taxiservice.controller;

import com.taxiservice.model.Role;
import com.taxiservice.model.User;
import com.taxiservice.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * Контроллер аутентификации и регистрации.
 * Обрабатывает вход в систему, регистрацию новых пользователей
 * и перенаправление на панель управления в зависимости от роли.
 *
 * @version 1.0
 */
@Controller
public class AuthController {

    private final UserService userService;

    /**
     * Конструктор с внедрением зависимости.
     *
     * @param userService сервис управления пользователями
     */
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Отображение страницы входа в систему.
     *
     * @return имя шаблона страницы входа
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * Отображение страницы регистрации.
     *
     * @param model модель для передачи данных в шаблон
     * @return имя шаблона страницы регистрации
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    /**
     * Обработка регистрации нового пользователя.
     * По умолчанию новому пользователю присваивается роль CLIENT.
     *
     * @param user               данные нового пользователя из формы
     * @param redirectAttributes атрибуты для передачи flash-сообщений
     * @return перенаправление на страницу входа при успехе или обратно на регистрацию при ошибке
     */
    @PostMapping("/register")
    public String register(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            user.setRole(Role.CLIENT);
            userService.register(user);
            redirectAttributes.addFlashAttribute("success",
                    "Регистрация прошла успешно! Войдите в систему.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка при регистрации. Попробуйте ещё раз.");
            return "redirect:/register";
        }
    }

    /**
     * Перенаправление на панель управления в зависимости от роли текущего пользователя.
     * <ul>
     *     <li>CLIENT — на список заказов клиента</li>
     *     <li>DRIVER — на список заказов водителя</li>
     *     <li>DISPATCHER — на список заказов диспетчера</li>
     *     <li>ADMIN — на управление пользователями</li>
     * </ul>
     *
     * @param principal текущий аутентифицированный пользователь
     * @return перенаправление на соответствующую страницу
     */
    @GetMapping("/dashboard")
    public String dashboard(Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            User user = userService.findByUsername(principal.getName())
                    .orElse(null);

            if (user == null) {
                return "redirect:/login";
            }

            switch (user.getRole()) {
                case CLIENT:
                    return "redirect:/client/orders";
                case DRIVER:
                    return "redirect:/driver/orders";
                case DISPATCHER:
                    return "redirect:/dispatcher/orders";
                case ADMIN:
                    return "redirect:/admin/users";
                default:
                    return "redirect:/login";
            }
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    /**
     * Отображение страницы «О системе».
     *
     * @return имя шаблона страницы «О системе»
     */
    @GetMapping("/about")
    public String aboutPage() {
        return "about";
    }
}

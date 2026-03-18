package com.taxiservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Глобальный обработчик исключений.
 * Перехватывает ошибки во всех контроллерах и возвращает
 * соответствующие страницы ошибок с информативными сообщениями.
 *
 * @version 1.0
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обработка ошибки 404 — страница не найдена.
     *
     * @param ex    исключение NoHandlerFoundException
     * @param model модель для передачи данных в шаблон
     * @return имя шаблона страницы ошибки
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoHandlerFoundException ex, Model model) {
        model.addAttribute("error", "Страница не найдена");
        model.addAttribute("message", "Запрошенная страница не существует: " + ex.getRequestURL());
        model.addAttribute("status", 404);
        return "error";
    }

    /**
     * Обработка ошибки IllegalArgumentException — некорректные аргументы.
     *
     * @param ex    исключение IllegalArgumentException
     * @param model модель для передачи данных в шаблон
     * @return имя шаблона страницы ошибки
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(IllegalArgumentException ex, Model model) {
        model.addAttribute("error", "Некорректный запрос");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("status", 400);
        return "error";
    }

    /**
     * Обработка ошибки доступа — SecurityException.
     *
     * @param ex    исключение SecurityException
     * @param model модель для передачи данных в шаблон
     * @return имя шаблона страницы ошибки
     */
    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleForbidden(SecurityException ex, Model model) {
        model.addAttribute("error", "Доступ запрещён");
        model.addAttribute("message", "У вас нет прав для выполнения данного действия.");
        model.addAttribute("status", 403);
        return "error";
    }

    /**
     * Обработка RuntimeException — ошибки времени выполнения.
     *
     * @param ex    исключение RuntimeException
     * @param model модель для передачи данных в шаблон
     * @return имя шаблона страницы ошибки
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleRuntimeException(RuntimeException ex, Model model) {
        model.addAttribute("error", "Внутренняя ошибка сервера");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("status", 500);
        return "error";
    }

    /**
     * Обработка всех прочих исключений.
     *
     * @param ex    исключение Exception
     * @param model модель для передачи данных в шаблон
     * @return имя шаблона страницы ошибки
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception ex, Model model) {
        model.addAttribute("error", "Произошла ошибка");
        model.addAttribute("message", "Что-то пошло не так. Пожалуйста, попробуйте позже.");
        model.addAttribute("status", 500);
        return "error";
    }
}

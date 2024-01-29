package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@ControllerAdvice("ru.yandex.practicum.filmorate")
@RestControllerAdvice
public class ErrorHandler {
    // Отлавливаем все ValidationException
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleNotValid(final ValidationException e) {
        log.error("Получен статус 400 Bad Request. {}", e.getMessage(), e);
        return Map.of("errorMessage", e.getMessage());
    }

    // Отлавливаем все NotFoundException
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(final NotFoundException e) {
        log.error("Получен статус 404 Not found. {}", e.getMessage(), e);
        return Map.of("errorMessage", e.getMessage());
    }

    // Отлавливаем все остальные возможные ошибки.
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleAllException(final Exception e) {
        log.error("Получен статус 500 Internal Server Error. {}", e.getMessage(), e);
        return Map.of("errorMessage", e.getMessage());
    }
}

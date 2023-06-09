package ru.vsu.cs.picstorm.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.vsu.cs.picstorm.dto.response.ErrorDto;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class ExceptionHandlerController {
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDto handleRuntimeException(RuntimeException e) {
        return new ErrorDto(e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorDto handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return new ErrorDto(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorDto handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        StringBuilder builder = new StringBuilder();
        ex.getBindingResult().getAllErrors().forEach((error) -> builder.append(error.getDefaultMessage()).append("\n"));
        return new ErrorDto(builder.toString());
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorDto handleValidationException(ValidationException e) {
        return new ErrorDto(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorDto handleIllegalArgumentException(IllegalArgumentException e) {
        return new ErrorDto(e.getMessage());
    }

    @ExceptionHandler(JWTVerificationException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public ErrorDto handleJWTVerificationException(JWTVerificationException e) {
        return new ErrorDto(e.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public ErrorDto handleAuthenticationException(AuthenticationException e) {
        return new ErrorDto("Неверное имя или пароль");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public ErrorDto handleAccessDeniedException(AccessDeniedException e) {
        return new ErrorDto(e.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ErrorDto handleNoSuchElementException(NoSuchElementException e) {
        return new ErrorDto(e.getMessage());
    }
}

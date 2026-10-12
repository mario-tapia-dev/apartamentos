package com.blue.apartamentos.exceptions;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ApiError(LocalDateTime timestamp, int status, String mensaje, List<String> detalles) {
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String mensaje, List<String> detalles) {
        return ResponseEntity.status(status)
                .body(new ApiError(LocalDateTime.now(), status.value(), mensaje, detalles));
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> noEncontrado(RecursoNoEncontradoException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ApiError> reglaNegocio(ReglaNegocioException ex) {
        return build(ex.getStatus(), ex.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Datos inválidos", detalles);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> validacionJpa(ConstraintViolationException ex) {
        List<String> detalles = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Datos inválidos", detalles);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integridad(DataIntegrityViolationException ex) {
        return build(HttpStatus.CONFLICT,
                "Operación rechazada por la base de datos: valor duplicado (email, INE...) "
                        + "o el registro tiene información relacionada", List.of());
    }

    @ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class })
    public ResponseEntity<ApiError> peticionMalformada(Exception ex) {
        return build(HttpStatus.BAD_REQUEST,
                "Petición malformada: revisa el JSON, las fechas (yyyy-MM-dd), los valores de enums y los parámetros",
                List.of());
    }
}

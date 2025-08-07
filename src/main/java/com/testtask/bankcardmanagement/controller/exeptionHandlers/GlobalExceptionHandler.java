package com.testtask.bankcardmanagement.controller.exeptionHandlers;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.testtask.bankcardmanagement.exception.other.InvalidSortFieldException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Order(value = Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {

// ALL

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAll(Exception exception) {
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(exception.getMessage());
    }

// Validation

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getBindingResult().getAllErrors().stream()
                .collect(Collectors.toMap(
                        error -> ((FieldError) error).getField(),
                        error -> error.getDefaultMessage(),
                        (existing, replacement) -> replacement,
                        HashMap::new
                )));
    }

// Other

    @ExceptionHandler(InvalidSortFieldException.class)
    public ResponseEntity<String> handleInvalidSortField(InvalidSortFieldException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    // Catching enum troubles from request body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
        Throwable cause = exception.getCause();
        // If InvalidFormatException (thrown by jenkins) and it is enum type
        if (cause instanceof InvalidFormatException ifx && ifx.getTargetType() != null && ifx.getTargetType().isEnum()) {
            String field = ifx.getPath() != null && !ifx.getPath().isEmpty() ? ifx.getPath().get(0).getFieldName() : "unknown enum field";
            Object[] enumConstants = ifx.getTargetType().getEnumConstants();
            String allowed = enumConstants != null ? Arrays.toString(enumConstants) : "";
            String value = String.valueOf(ifx.getValue());

            String msg = String.format("Invalid value '%s' for field '%s'. Allowed values: %s.", value, field, allowed);
            return ResponseEntity.badRequest().body(msg);
        }

        // Not enum reason
        return ResponseEntity.badRequest().body("Malformed request: " + exception.getMessage());
    }

    // Catching enum troubles from: path variable or request param
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        // MethodArgumentTypeMismatchException contains the reason to which class the conversion failed
        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            String allowed = Arrays.toString(ex.getRequiredType().getEnumConstants());
            String msg = String.format(
                    "Invalid value '%s' for enum '%s'. Allowed values: %s.",
                    ex.getValue(), ex.getRequiredType().getSimpleName(), allowed
            );

            return ResponseEntity.badRequest().body(msg);
        }

        // Not enum reason
        return ResponseEntity.badRequest().body("Invalid parameter: " + ex.getMessage());
    }
}

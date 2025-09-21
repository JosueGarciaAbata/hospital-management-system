package com.hospital.admin_service.rest;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({ MethodArgumentNotValidException.class, BindException.class })
    public ResponseEntity<?> handleValidation(Exception ex) {
        var binding = ex instanceof MethodArgumentNotValidException manv
                ? manv.getBindingResult()
                : ((BindException) ex).getBindingResult();

        Map<String, String> errors = binding.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        DefaultMessageSourceResolvable::getDefaultMessage,
                        (a, b) -> a
                ));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "mensaje", "Datos inválidos",
                "errores", errors
        ));
    }
}

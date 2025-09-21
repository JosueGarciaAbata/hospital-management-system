package com.hospital.exceptions;

import feign.FeignException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalHandlerException {

    // Errores Feign (cuando falla otro microservicio)
    @ExceptionHandler(FeignException.class)
    public ProblemDetail handleFeignException(FeignException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        problem.setTitle("Error en comunicación con otro servicio");
        problem.setDetail("El servicio remoto no está disponible en este momento.");
        return problem;
    }

    // Errores de validación (Hibernate Validator)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Error de validación");

        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        problem.setProperty("errors", errors);
        return problem;
    }

    // Recurso no encontrado (entidades)
    @ExceptionHandler({RoleNotFoundException.class, UserNotFoundException.class, CenterIdNotFoundException.class})
    public ProblemDetail handleNotFound(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Recurso no encontrado");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    // Validaciones personalizadas
    @ExceptionHandler({DniAlreadyExistsException.class})
    public ProblemDetail handleDniAlreadyExists(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Error de validación personalizada");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    // Restricciones de base de datos
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Violación de integridad de datos");
        problem.setDetail("No se puede completar la operación debido a restricciones en la base de datos.");
        return problem;
    }

    // Fallback general
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Error inesperado");
        problem.setDetail("Ha ocurrido un error interno, contacte al administrador.");
        problem.setProperty("error", ex.getMessage());
        return problem;
    }
}

package com.hospital.exceptions;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.batch.BatchTaskExecutor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Errores Feign (cuando falla otro microservicio)
    @ExceptionHandler(FeignException.class)
    public ProblemDetail handleFeignException(FeignException ex) {
        log.error("FeignException: Error calling remote service -> {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        problem.setTitle("Error en comunicación con otro servicio");
        problem.setDetail("El servicio remoto no está disponible en este momento.");
        return problem;
    }

    // Errores de validación (Hibernate Validator)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {} errors", ex.getBindingResult().getErrorCount());
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
    @ExceptionHandler({RoleNotFoundException.class,
            UserNotFoundException.class,
            CenterIdNotFoundException.class,
            UserByDniNotFoundException.class,
            TokenNotFoundException.class})
    public ProblemDetail handleNotFound(Exception ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Recurso no encontrado");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    // Validaciones personalizadas
    @ExceptionHandler({DniAlreadyExistsException.class})
    public ProblemDetail handleAlreadyExists(Exception ex) {
        log.warn("Duplicate validation error: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Error de validación de duplicacion");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler({InvalidTokenException.class})
    public ProblemDetail handleInvalidToken(Exception ex) {
        log.warn("Invalid token error: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Error de token inválido");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(EmailTemplateException.class)
    public ProblemDetail handleEmailTemplateException(EmailTemplateException ex) {
        log.error("Email template error: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Error en la plantilla de correo");
        problem.setDetail("Hubo un problema al procesar la plantilla de correo: " + ex.getMessage());
        return problem;
    }

    @ExceptionHandler(EmailSendingException.class)
    public ProblemDetail handleEmailSendingException(EmailSendingException ex) {
        log.error("Email sending error: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Error al enviar correo");
        problem.setDetail("No se pudo enviar el correo: " + ex.getMessage());
        return problem;
    }

    // Restricciones de base de datos
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Violación de integridad de datos");
        problem.setDetail("No se puede completar la operación debido a restricciones en la base de datos: " + ex.getMostSpecificCause().getMessage());
        return problem;
    }


    // Fallback general
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Error inesperado");
        problem.setDetail("Ha ocurrido un error interno, contacte al administrador: " + ex.getMessage());
        return problem;
    }
}

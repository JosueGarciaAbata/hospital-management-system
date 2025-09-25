package com.hospital.exceptions;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ProblemDetail buildProblem(HttpStatus status, String title, String detail, Map<String, String> errors) {
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setTitle(title);
        problem.setDetail(detail);
        if (errors != null && !errors.isEmpty()) {
            problem.setProperty("errors", errors);
        }
        return problem;
    }

    // Errores Feign (cuando falla otro microservicio)
    @ExceptionHandler(FeignException.class)
    public ProblemDetail handleFeignException(FeignException ex) {
        log.error("FeignException: {}", ex.getMessage());
        return buildProblem(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Error en comunicación con otro servicio",
                "El servicio remoto no está disponible en este momento.",
                Map.of("global", "Servicio remoto no disponible")
        );
    }

    @ExceptionHandler(ForbiddenException.class)
    public ProblemDetail handleForbidden(ForbiddenException ex) {
        return buildProblem(HttpStatus.FORBIDDEN, "Acceso prohibido",
                ex.getMessage(), Map.of("global", ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnauthorized(UnauthorizedException ex) {
        return buildProblem(HttpStatus.UNAUTHORIZED, "No autorizado",
                ex.getMessage(), Map.of("global", ex.getMessage()));
    }

    // Errores de validación (Hibernate Validator)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {} errors", ex.getBindingResult().getErrorCount());
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return buildProblem(HttpStatus.BAD_REQUEST, "Error de validación", "Hay campos con errores", errors);
    }

    // Recurso no encontrado (entidades)
    @ExceptionHandler({
            RoleNotFoundException.class,
            UserNotFoundException.class,
            CenterIdNotFoundException.class,
            UserByDniNotFoundException.class,
            TokenNotFoundException.class})
    public ProblemDetail handleNotFound(Exception ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildProblem(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(), Map.of("global", ex.getMessage()));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ProblemDetail handleInvalidToken(InvalidTokenException ex) {
        log.warn("Invalid token error: {}", ex.getMessage());
        return buildProblem(HttpStatus.BAD_REQUEST, "Error de token inválido", ex.getMessage(), Map.of("token", ex.getMessage()));
    }

    @ExceptionHandler(EmailTemplateException.class)
    public ProblemDetail handleEmailTemplateException(EmailTemplateException ex) {
        log.error("Email template error: {}", ex.getMessage());
        return buildProblem(HttpStatus.INTERNAL_SERVER_ERROR, "Error en la plantilla de correo", ex.getMessage(), Map.of("global", ex.getMessage()));
    }

    @ExceptionHandler(EmailSendingException.class)
    public ProblemDetail handleEmailSendingException(EmailSendingException ex) {
        log.error("Email sending error: {}", ex.getMessage());
        return buildProblem(HttpStatus.INTERNAL_SERVER_ERROR, "Error al enviar correo", ex.getMessage(), Map.of("global", ex.getMessage()));
    }

    // Integridad de datos personalizada
    @ExceptionHandler({
            DoctorAssignedException.class,
            SelfDeletionNotAllowedException.class
    })
    public ProblemDetail handleDataIntegrity(Exception ex) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        return buildProblem(HttpStatus.CONFLICT, "Violación de integridad de datos", ex.getMessage(), Map.of("global", ex.getMessage()));
    }

    // Integridad de datos personalizadas
    @ExceptionHandler(DniAlreadyExistsException.class)
    public ProblemDetail handleDniAlreadyExists(Exception ex) {
        log.warn("Duplicate validation error: {}", ex.getMessage());
        return buildProblem(HttpStatus.BAD_REQUEST, "Error de validación", ex.getMessage(), Map.of("username", ex.getMessage()));
    }

    // Integridad de datos personalizadas
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExists(Exception ex) {
        log.warn("Duplicate validation error: {}", ex.getMessage());
        return buildProblem(HttpStatus.BAD_REQUEST, "Error de validación", ex.getMessage(), Map.of("email", ex.getMessage()));
    }

    // Restricciones de base de datos
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMessage());
        String message = "No se puede completar la operación debido a restricciones en la base de datos: "
                + ex.getMostSpecificCause().getMessage();
        return buildProblem(HttpStatus.CONFLICT, "Violación de integridad de datos", message, Map.of("global", message));
    }

    // Fallback general
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);
        return buildProblem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error inesperado",
                "Ha ocurrido un error interno, contacte al administrador: " + ex.getMessage(),
                Map.of("global", "Error interno del servidor")
        );
    }
}

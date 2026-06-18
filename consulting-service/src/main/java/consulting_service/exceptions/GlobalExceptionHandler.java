package consulting_service.exceptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;



@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String[]> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().toArray(new String[0])
                ));

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Algunos campos no son válidos"
        );
        problemDetail.setTitle("Validation Failed");
        problemDetail.setType(URI.create("https://example.com/validation-error"));
        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problemDetail.setTitle("Recurso no encontrado");
        problemDetail.setType(URI.create("https://example.com/not-found"));

        return problemDetail;
    }

    @ExceptionHandler(DuplicateDniException.class)
    public ProblemDetail handleDuplicateDni(DuplicateDniException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "El DNI ya existe en el sistema"
        );
        problemDetail.setTitle("DNI duplicado");
        problemDetail.setType(URI.create("https://example.com/duplicate-dni"));

        return problemDetail;
    }

    @ExceptionHandler(PatientHasConsultationsException.class)
    public ProblemDetail handlePatientHasConsultations(PatientHasConsultationsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problemDetail.setTitle("Eliminación no permitida");
        problemDetail.setType(URI.create("https://example.com/patient-has-consultations"));

        return problemDetail;
    }
}
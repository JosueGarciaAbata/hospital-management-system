package consulting_service.exceptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;


@RestControllerAdvice
public class GlobalExceptionHandler {

    // Errores de validación de DTO
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex) {

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

        ApiError apiError = new ApiError(
                "https://example.com/validation-error",
                "Validation Failed",
                HttpStatus.BAD_REQUEST.value(),
                "Algunos campos no son válidos",
                ex.getParameter().getMethod().getName(),
                errors
        );

        return ResponseEntity.badRequest().body(apiError);
    }

//    // Ejemplo de error de negocio tipo token expirado
//    @ExceptionHandler(TokenExpiredException.class)
//    public ResponseEntity<ApiError> handleTokenExpired(TokenExpiredException ex) {
//        ApiError apiError = new ApiError(
//                "https://example.com/auth-error",
//                "Token Expired",
//                HttpStatus.UNAUTHORIZED.value(),
//                ex.getMessage(),
//                "/auth/login"
//        );
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
//    }


}
package consulting_service.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EcuadorianDniValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface EcuadorianDni {
    String message() default "Cédula ecuatoriana no válida";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

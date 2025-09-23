package consulting_service.security.aspects;

import consulting_service.security.annotations.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

@Aspect
@Component
public class RoleCheckAspect {

    @Around("@annotation(rolesAllowed)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RolesAllowed rolesAllowed) throws Throwable {
        var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No request context");
        }

        var request = attrs.getRequest();
        String rolesHeader = request.getHeader("X-Roles");
        if (rolesHeader == null || rolesHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Falta cabecera X-Roles");
        }

        // normalizar
        var userRoles = Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(s -> !s.isBlank())
                .toList();

        var required = Arrays.stream(rolesAllowed.value())
                .map(String::trim)
                .map(String::toUpperCase)
                .toArray(String[]::new);

        // ✅ OR (al menos uno)
        boolean allowed = Arrays.stream(required).anyMatch(userRoles::contains);

        if (!allowed) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario no cuenta con el/los roles necesarios para acceder a este endpoint"
            );
        }

        return joinPoint.proceed();
    }
}

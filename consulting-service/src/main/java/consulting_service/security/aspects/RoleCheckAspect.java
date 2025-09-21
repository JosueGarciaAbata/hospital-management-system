package consulting_service.security.aspects;

import consulting_service.security.annotations.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;

@Component
@Aspect
public class RoleCheckAspect {

    @Around("@annotation(rolesAllowed)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RolesAllowed rolesAllowed) throws Throwable {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attrs.getRequest();

        String rolesHeader = request.getHeader("X-Roles");

        List<String> userRoles = Arrays.asList(rolesHeader.split(","));
        boolean allowed = Arrays.stream(rolesAllowed.value())
                .allMatch(userRoles::contains);
               //si se necesita al menos uno entonces cambiar por any
        if (!allowed) {
            throw new RuntimeException("El usuario no cuenta con el/los roles necesarios para acceder a este endpoint");
        }

        return joinPoint.proceed();
    }

}
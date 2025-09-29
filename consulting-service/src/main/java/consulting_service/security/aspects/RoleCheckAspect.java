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

@Component
@Aspect
public class RoleCheckAspect {

    @Around("@annotation(rolesAllowed)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RolesAllowed rolesAllowed) throws Throwable {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "No se pudo obtener el contexto de la solicitud"
                );
            }
            
            HttpServletRequest request = attrs.getRequest();
            String rolesHeader = request.getHeader("X-Roles");
            
            // Log para depuración
            System.out.println("Headers de la solicitud:");
            request.getHeaderNames().asIterator().forEachRemaining(headerName -> 
                System.out.println(headerName + ": " + request.getHeader(headerName))
            );
            
            if (rolesHeader == null || rolesHeader.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Header X-Roles no encontrado en la solicitud"
                );
            }
            
            List<String> userRoles = Arrays.asList(rolesHeader.split(","));
            boolean allowed = Arrays.stream(rolesAllowed.value())
                    .anyMatch(userRoles::contains);
            
            if (!allowed) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "El usuario no cuenta con el/los roles necesarios para acceder a este endpoint"
                );
            }
            
            return joinPoint.proceed();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error en la verificación de roles: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al verificar los roles: " + e.getMessage()
            );
        }

    }

}
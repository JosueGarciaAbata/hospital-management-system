package com.hospital.admin_service.security.filters;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Aspect
@Component
public class RoleAspect {

    private final HttpServletRequest request;

    public RoleAspect(HttpServletRequest request) {
        this.request = request;
    }

    @Before("@within(com.hospital.admin_service.security.filters.RequireRole) || " +
            "@annotation(com.hospital.admin_service.security.filters.RequireRole)")
    public void checkRole(JoinPoint jp) {
        MethodSignature sig = (MethodSignature) jp.getSignature();
        RequireRole ann = AnnotationUtils.findAnnotation(sig.getMethod(), RequireRole.class);
        if (ann == null) {
            ann = AnnotationUtils.findAnnotation(sig.getDeclaringType(), RequireRole.class);
        }

        String rolesHeader = request.getHeader("X-Roles");
        if (rolesHeader == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing X-Roles");
        }
        Set<String> roles = Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        boolean allowed = Arrays.stream(ann.value()).anyMatch(roles::contains);
        if (!allowed) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Required role: " + String.join(" OR ", ann.value())
            );
        }
    }
}

package com.hospital.security.aop;

import com.hospital.exceptions.ForbiddenException;
import com.hospital.exceptions.UnauthorizedException;
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

    @Before("@within(com.hospital.security.aop.RequireRole) || " +
            "@annotation(com.hospital.security.aop.RequireRole)")
    public void checkRole(JoinPoint jp) {
        MethodSignature sig = (MethodSignature) jp.getSignature();
        RequireRole ann = AnnotationUtils.findAnnotation(sig.getMethod(), RequireRole.class);
        if (ann == null) {
            ann = AnnotationUtils.findAnnotation(sig.getDeclaringType(), RequireRole.class);
        }

        String rolesHeader = request.getHeader("X-Roles");
        if (rolesHeader == null) {
            throw new UnauthorizedException("Missing X-Roles");
        }
        Set<String> roles = Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        boolean allowed = Arrays.stream(ann.value()).anyMatch(roles::contains);
        if (!allowed) {
            throw new ForbiddenException("Required role: " + String.join(" OR ", ann.value())
            );
        }
    }
}

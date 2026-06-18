package com.drtx.jdit.reportservice.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Log para depuración
        logger.info("JwtAuthenticationFilter procesando solicitud para: " + request.getRequestURI());
        logHeaders(request);

        String token = extractToken(request);

        if (token != null) {
            try {
                // Extraemos el userId y roles de los headers que envía el gateway
                String userId = request.getHeader("X-User-Id");
                String rolesString = request.getHeader("X-Roles");

                if (userId != null && rolesString != null) {
                    List<SimpleGrantedAuthority> authorities = Arrays.stream(rolesString.split(","))
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.trim()))
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, token, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    logger.warn("Headers de autenticación no encontrados. X-User-Id: " + userId + ", X-Roles: " + rolesString);
                }
            } catch (Exception e) {
                logger.error("Error al procesar la autenticación", e);
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private void logHeaders(HttpServletRequest request) {
        logger.info("Headers de la solicitud:");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            logger.info(headerName + ": " + request.getHeader(headerName));
        }
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

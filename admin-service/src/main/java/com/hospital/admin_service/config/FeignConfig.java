// admin_service/src/main/java/com/hospital/admin_service/config/FeignConfig.java
package com.hospital.admin_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableFeignClients(basePackages = "com.hospital.admin_service.external")
public class FeignConfig {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(FeignConfig.class);

    @Bean
    public feign.codec.ErrorDecoder errorDecoder(ObjectMapper mapper) {
        return new RemoteErrorDecoder(mapper);
    }

    /**
     * Nivel de logging de Feign (usa SLF4J). FULL = request y response completas.
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * Interceptor para:
     * 1) Reenviar X-Roles
     * 2) Loggear cómo se está enviando la petición (método, URL, headers y body)
     */
    @Bean
    public RequestInterceptor relayHeadersInterceptor() {
        return (RequestTemplate template) -> {
            var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();

                String roles = req.getHeader("X-Roles");
                if (roles != null && !roles.isBlank()) {
                    template.header("X-Roles", roles);
                }

                // (Opcional) Propagar un correlativo si lo tienes en el request entrante
                String traceId = req.getHeader("X-Trace-Id");
                if (traceId != null && !traceId.isBlank()) {
                    template.header("X-Trace-Id", traceId);
                    MDC.put("traceId", traceId); // para que salga en el patrón de log
                }
            }

            // ---- LOG DE LA PETICIÓN FEIGN ------------------------------------
            try {
                String method = template.method();
                String url = template.url();

                // Headers como "nombre: valor1,val2"
                String headers = template.headers().entrySet().stream()
                        .map(e -> e.getKey() + ": " + String.join(",", e.getValue()))
                        // Oculta sensibles
                        .map(line -> maskIfSensitive(line))
                        .collect(Collectors.joining(", "));

                String body = null;
                if (template.requestBody() != null && template.requestBody().asBytes() != null) {
                    body = new String(template.requestBody().asBytes(), StandardCharsets.UTF_8);
                    // Evitar loggear binarios y cosas sensibles
                    if (body.length() > 5000) {
                        body = body.substring(0, 5000) + " ...[truncated]";
                    }
                }

                log.info("FEIGN REQUEST -> {} {} | headers=[{}]{}",
                        method, url, headers,
                        body != null && !body.isBlank() ? " | body=" + body : "");

            } catch (Exception ex) {
                log.warn("No se pudo loggear la petición Feign: {}", ex.getMessage());
            }
            // ------------------------------------------------------------------
        };
    }

    private static String maskIfSensitive(String headerLine) {
        // Enmascara Authorization, Cookie, Set-Cookie, etc.
        String lower = headerLine.toLowerCase();
        if (lower.startsWith("authorization:")
                || lower.startsWith("cookie:")
                || lower.startsWith("set-cookie:")
                || lower.startsWith("x-api-key:")
                || lower.startsWith("x-access-token:")) {
            int idx = headerLine.indexOf(':');
            return headerLine.substring(0, idx + 1) + " ******";
        }
        return headerLine;
    }
}

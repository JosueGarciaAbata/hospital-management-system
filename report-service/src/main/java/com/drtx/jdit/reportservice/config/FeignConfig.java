package com.drtx.jdit.reportservice.config;

import feign.Logger;
import feign.codec.ErrorDecoder;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración de Feign con manejo de errores y logging completo
 */
@Configuration
@Slf4j
public class FeignConfig {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            String requestUrl = response.request().url();
            int status = response.status();

            Map<String, Object> diagnosticInfo = new HashMap<>();
            diagnosticInfo.put("methodKey", methodKey);
            diagnosticInfo.put("url", requestUrl);
            diagnosticInfo.put("status", status);
            diagnosticInfo.put("reason", response.reason());
            diagnosticInfo.put("headers", response.headers());

            Map<String, Object> requestHeaders = new HashMap<>();
            response.request().headers().forEach((key, values) ->
                    requestHeaders.put(key, String.join(", ", values)));
            diagnosticInfo.put("requestHeaders", requestHeaders);

            try {
                String responseBody = response.body() != null ?
                        new String(response.body().asInputStream().readAllBytes()) : "No response body";
                diagnosticInfo.put("responseBody", responseBody);

                if (status >= 500) {
                    // log.error("ERROR DE SERVIDOR en llamada Feign: {}", diagnosticInfo);
                } else if (status >= 400) {
                    // log.warn("ERROR DE CLIENTE en llamada Feign: {}", diagnosticInfo);
                }

            } catch (Exception e) {
                // log.error("Error al procesar respuesta de error Feign", e);
                diagnosticInfo.put("errorProcessingResponse", e.getMessage());
            }

            switch (status) {
                case 401 -> {
                    return new FeignException.Unauthorized("Autenticación requerida: " + methodKey, response.request(), null, null);
                }
                case 403 -> {
                    return new FeignException.Forbidden("Acceso denegado: " + methodKey, response.request(), null, null);
                }
                case 404 -> {
                    return new FeignException.NotFound("Recurso no encontrado: " + methodKey, response.request(), null, null);
                }
                default -> {
                    if (status >= 500) return new FeignException.InternalServerError("Error en el servicio externo: " + response.reason(), response.request(), null, null);
                }
            }

            return FeignException.errorStatus(methodKey, response);
        };
    }
}

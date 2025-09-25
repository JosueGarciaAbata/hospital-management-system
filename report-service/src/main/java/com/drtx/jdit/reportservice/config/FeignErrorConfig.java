package com.drtx.jdit.reportservice.config;

import feign.codec.ErrorDecoder;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class FeignErrorConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            String requestUrl = response.request().url();
            int status = response.status();
            
            try {
                // Intentar leer el cuerpo de la respuesta para más detalles
                String responseBody = response.body() != null ? 
                    new String(response.body().asInputStream().readAllBytes()) : "No response body";
                
                log.error("Error en llamada Feign - Método: {}, URL: {}, Status: {}, Respuesta: {}", 
                         methodKey, requestUrl, status, responseBody);
                
            } catch (Exception e) {
                log.error("Error al procesar respuesta de error Feign", e);
            }
            
            // Devolver la excepción original para mantener el comportamiento estándar
            return FeignException.errorStatus(methodKey, response);
        };
    }
}
package com.drtx.jdit.reportservice.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador para verificar la salud del servicio
 */
@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "Endpoints para verificar el estado del servicio de reportes")
public class HealthController {

    @GetMapping
    @Operation(
            summary = "Verificar salud del servicio",
            description = "Devuelve un mensaje simple confirmando que el servicio de reportes está en ejecución."
    )
    @ApiResponse(responseCode = "200", description = "El servicio está funcionando correctamente")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Report Service is up and running!");
    }
}

package com.hospital.feign;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceWrapper {

    private final AdminClient adminClient;

    public AdminServiceWrapper(AdminClient adminClient) {
        this.adminClient = adminClient;
    }

    @CircuitBreaker(name = "adminService", fallbackMethod = "validateCenterIdFallback")
    public ResponseEntity<Void> validateCenterId(Long id) {
        return adminClient.validateCenterId(id);
    }

    // El servicio remoto no esta disponible, no es un error de negocio, sino un error tecnico de infraestructura.
    public ResponseEntity<Void> validateCenterIdFallback(Long id, Throwable throwable) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}

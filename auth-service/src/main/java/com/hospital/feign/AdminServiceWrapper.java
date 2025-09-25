package com.hospital.feign;

import com.hospital.dtos.MedicalCenterDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @CircuitBreaker(name = "adminService", fallbackMethod = "getCentersByIdFallback")
    public List<MedicalCenterDto> getCentersById(List<Long> ids, boolean includeDeleted) {
        return adminClient.getCentersByIds(ids, includeDeleted);
    }
    // Se puede devolver un objeto dummy, o devolver null. En caso de devolver null, hay que manejarlo en el servicio que lo llama.
    // Por el tiempo, lo dejo con un objeto dummy. Otra opcion es lanzar una excepcion personalizada, que indique que el servicio no esta disponible.
    public  List<MedicalCenterDto>  getCentersByIdFallback(List<Long> ids, boolean includedDeleted, Throwable throwable) {
        return  List.of();
    }

    @CircuitBreaker(name = "adminService", fallbackMethod = "existsByUserIdFallback")
    public ResponseEntity<Void> existsByUserId(Long userId) {
        return adminClient.existsByUserId(userId);
    }

    public ResponseEntity<Void> existsByUserIdFallback(Long userId, Throwable throwable) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }


}

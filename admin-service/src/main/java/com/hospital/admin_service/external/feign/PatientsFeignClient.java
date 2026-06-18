package com.hospital.admin_service.external.feign;

import com.hospital.admin_service.config.FeignConfig;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "consulting-service",
        contextId = "patientsClient",
        path = "/api/consulting/patients",
        configuration = FeignConfig.class,
        dismiss404 = true
)
public interface PatientsFeignClient {

    @GetMapping("/center-has-patients/{centerId}")
    @CircuitBreaker(name = "consultingService") @Retry(name = "consultingService")
    ResponseEntity<Void> checkCenter(@PathVariable("centerId") Long centerId);
}

package com.hospital.admin_service.external.feign;

import com.hospital.admin_service.config.FeignConfig;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "consulting-service",
        contextId = "consultationsClient",
        path = "/api/consulting/medical-consultations",
        configuration = FeignConfig.class,
        dismiss404 = true
)
public interface ConsultingFeignClient {

    @GetMapping("/center-has-consultations/{centerId}")
    @CircuitBreaker(name = "consultingService") @Retry(name = "consultingService")
    ResponseEntity<Void> checkCenter(@PathVariable("centerId") Long centerId);

    @GetMapping("/doctor-has-consultations/{doctorId}")
    @CircuitBreaker(name = "consultingService") @Retry(name = "consultingService")
    ResponseEntity<Void> checkDoctor(@PathVariable("doctorId") Long doctorId);
}

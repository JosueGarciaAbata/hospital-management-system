package com.hospital.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Fallback --> una accion alternativa en caso de que el servicio falle.
@FeignClient(name = "admin-service", fallback = AdminClientFallback.class)
public interface AdminClient {

    @GetMapping("/admin/centers/{id}")
    ResponseEntity<Void> validateCenterId(@PathVariable Long id);
}

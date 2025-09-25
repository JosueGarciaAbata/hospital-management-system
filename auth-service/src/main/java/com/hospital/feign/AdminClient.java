package com.hospital.feign;

import com.hospital.dtos.MedicalCenterDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Fallback --> una accion alternativa en caso de que el servicio falle.
@FeignClient(name = "admin-service", fallback = AdminClientFallback.class)
public interface AdminClient {

    @GetMapping("/admin/centers/validate/{id}")
    ResponseEntity<Void> validateCenterId(@PathVariable Long id);

    @PostMapping("/admin/centers/batch")
    List<MedicalCenterDto> getCentersByIds(@RequestBody List<Long> ids,
                                           @RequestParam(value = "includeDeleted", defaultValue = "false") boolean includeDeleted);

}

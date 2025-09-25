package com.hospital.feign;

import com.hospital.dtos.MedicalCenterDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminClientFallback implements AdminClient {

    // Este fallback no sabe si el centerId existe o no, solo sabe que no pudo comunicarse con el servicio remoto.
    // Eso significa que es un problema tecnico, no de negocio.
    @Override
    public ResponseEntity<Void> validateCenterId(Long id) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    @Override
    public List<MedicalCenterDto> getCentersByIds(List<Long> ids, boolean includeDeleted) {
        return List.of();
    }

}

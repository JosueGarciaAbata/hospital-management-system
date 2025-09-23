package com.hospital.admin_service.external.impl;

import com.hospital.admin_service.external.feign.PatientsFeignClient;
import com.hospital.admin_service.external.port.IPatientClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class PatientClientFeign implements IPatientClient {

    private final PatientsFeignClient feign;

    @Override
    public boolean hasActivePatientsInCenter(Long centerId) {
        var resp = feign.checkCenter(centerId);
        return resp != null && resp.getStatusCode().is2xxSuccessful();
    }
}

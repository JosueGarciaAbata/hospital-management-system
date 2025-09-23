package com.hospital.admin_service.external.impl;

import com.hospital.admin_service.external.port.IConsultingClient;
import com.hospital.admin_service.external.feign.ConsultingFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConsultingClientFeign implements IConsultingClient {

    private final ConsultingFeignClient feign;

    @Override
    public boolean hasActiveAppointmentsInCenter(Long centerId) {
        var resp = feign.checkCenter(centerId);
        return is2xx(resp); // 2xx = “sí tiene”, null/404 = “no tiene”
    }

    @Override
    public boolean hasFutureAppointments(Long doctorId) {
        var resp = feign.checkDoctor(doctorId);
        return is2xx(resp);
    }

    private boolean is2xx(@Nullable ResponseEntity<?> resp) {
        return resp != null && resp.getStatusCode().is2xxSuccessful();
    }
}

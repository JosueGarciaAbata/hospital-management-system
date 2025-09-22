package com.hospital.admin_service.external.impl;

import com.hospital.admin_service.external.port.IConsultingClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConsultingClientDummy implements IConsultingClient {
    @Override
    public boolean hasActivePatientsInCenter(Long centerId) {
        log.warn("[ConsultingClientDummy] Returning false for patients centerId={}", centerId);
        return false; // dummy
    }

    @Override
    public boolean hasFutureAppointments(Long doctorId) {
        log.warn("[ConsultingClientDummy] Returning false for doctor doctorId={}", doctorId);
        return false;
    }

    @Override
    public boolean hasFutureAppointmentsInCenter(Long centerId) {
        log.warn("[ConsultingClientDummy] Returning false for appointments centerId={}", centerId);
        return false; // dummy
    }
}

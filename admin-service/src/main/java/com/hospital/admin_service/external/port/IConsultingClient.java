package com.hospital.admin_service.external.port;

public interface IConsultingClient {

    boolean hasActivePatientsInCenter(Long centerId);
    boolean hasFutureAppointmentsInCenter(Long centerId);

    /**
     * Verifica si un doctor tiene consultas futuras.
     * @param doctorId id del doctor
     * @return true si existen consultas futuras, false si no
     */
    boolean hasFutureAppointments(Long doctorId);
}

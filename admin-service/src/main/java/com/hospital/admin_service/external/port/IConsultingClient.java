package com.hospital.admin_service.external.port;

public interface IConsultingClient {

    /**
     * Verifica si un centro médico tiene consultas activas o futuras registradas.
     *
     * @param centerId id del centro médico
     * @return true si existen consultas activas/futuras en el centro, false si no
     */
    boolean hasActiveAppointmentsInCenter(Long centerId);

    /**
     * Verifica si un doctor tiene consultas futuras.
     *
     * @param doctorId id del doctor
     * @return true si existen consultas futuras, false si no
     */
    boolean hasFutureAppointments(Long doctorId);
}

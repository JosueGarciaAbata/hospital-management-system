package com.hospital.admin_service.external.port;

public interface IPatientClient {
    /**
     * Verifica si un centro médico tiene pacientes activos (deleted=false).
     * @param centerId id del centro
     * @return true si hay pacientes; false si no (o 404 desde consulting-service)
     */
    boolean hasActivePatientsInCenter(Long centerId);
}

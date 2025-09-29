package consulting_service.exceptions;

public class PatientHasConsultationsException extends RuntimeException {
    public PatientHasConsultationsException(Long patientId) {
        super("El paciente con ID " + patientId + " tiene consultas médicas registradas y no puede ser eliminado.");
    }
}
package consulting_service.specifications;

import consulting_service.entities.MedicalConsultation;
import jakarta.persistence.criteria.Join;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j

/**
 * Especificaciones dinámicas para filtrar MedicalConsultation
 */
public class MedicalConsultationSpecifications {

    public static Specification<MedicalConsultation> dateGreaterThanOrEqual(LocalDateTime dateStart) {
        return (root, query, cb) -> dateStart == null ? null : cb.greaterThanOrEqualTo(root.get("date"), dateStart);
    }

    public static Specification<MedicalConsultation> dateLessThanOrEqual(LocalDateTime dateEnd) {
        return (root, query, cb) -> dateEnd == null ? null : cb.lessThanOrEqualTo(root.get("date"), dateEnd);
    }

    public static Specification<MedicalConsultation> hasDeletedStatus(Boolean deleted) {
        return (root, query, cb) -> deleted == null ? null : cb.equal(root.get("deleted"), deleted);
    }

    public static Specification<MedicalConsultation> centerIdIn(List<Long> centerIds) {
        return (root, query, cb) -> (centerIds == null || centerIds.isEmpty()) ? null : root.get("centerId").in(centerIds);
    }

    public static Specification<MedicalConsultation> doctorIdIn(List<Long> doctorIds) {
        return (root, query, cb) -> (doctorIds == null || doctorIds.isEmpty()) ? null : root.get("doctorId").in(doctorIds);
    }

    // Método para filtrar por especialidad
    public static Specification<MedicalConsultation> specialtyIdIn(List<Long> specialtyIds) {
        return (root, query, cb) -> {
            if (specialtyIds == null || specialtyIds.isEmpty()) return null;
            try {
                // Usa este join si tu MedicalConsultation tiene ManyToOne con Specialty
                Join<Object, Object> join = root.join("specialty"); // reemplaza "specialty" por el nombre exacto del atributo
                return join.get("id").in(specialtyIds);
            } catch (IllegalArgumentException iae) {
                // Si la entidad MedicalConsultation no tiene relación mapeada a Specialty, evitar lanzar la excepción
                // y devolver null para que esta condición no se aplicada por JPA Specification.
                log.warn("No se puede aplicar filtro por especialidad vía JPA Specification porque la entidad MedicalConsultation no tiene relación 'specialty' mapeada: {}", iae.getMessage());
                return null;
            }
        };
    }

    /**
     * Combina todos los filtros dinámicamente
     */
    public static Specification<MedicalConsultation> withFilters(
            LocalDateTime dateStart,
            LocalDateTime dateEnd,
            Boolean deleted,
            List<Long> centerIds,
            List<Long> doctorIds,
            List<Long> specialtyIds) {

        Specification<MedicalConsultation> spec =
                Specification.where(hasDeletedStatus(deleted != null ? deleted : Boolean.FALSE));

        if (dateStart != null) spec = spec.and(dateGreaterThanOrEqual(dateStart));
        if (dateEnd != null) spec = spec.and(dateLessThanOrEqual(dateEnd));
        if (centerIds != null && !centerIds.isEmpty()) spec = spec.and(centerIdIn(centerIds));
        if (doctorIds != null && !doctorIds.isEmpty()) spec = spec.and(doctorIdIn(doctorIds));
        if (specialtyIds != null && !specialtyIds.isEmpty()) spec = spec.and(specialtyIdIn(specialtyIds));

        return spec;
    }
}

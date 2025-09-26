package consulting_service.specifications;

import consulting_service.entities.MedicalConsultation;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Especificaciones para consultas dinámicas de MedicalConsultation
 * Soluciona el problema de PostgreSQL con parámetros null
 */
public class MedicalConsultationSpecifications {

    /**
     * Filtra por fecha de inicio (mayor o igual)
     */
    public static Specification<MedicalConsultation> dateGreaterThanOrEqual(LocalDateTime dateStart) {
        return (root, query, criteriaBuilder) -> {
            if (dateStart == null) {
                return null; // No aplica filtro
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("date"), dateStart);
        };
    }

    /**
     * Filtra por fecha de fin (menor o igual)
     */
    public static Specification<MedicalConsultation> dateLessThanOrEqual(LocalDateTime dateEnd) {
        return (root, query, criteriaBuilder) -> {
            if (dateEnd == null) {
                return null; // No aplica filtro
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("date"), dateEnd);
        };
    }

    /**
     * Filtra por estado deleted
     */
    public static Specification<MedicalConsultation> hasDeletedStatus(Boolean deleted) {
        return (root, query, criteriaBuilder) -> {
            if (deleted == null) {
                return null; // No aplica filtro
            }
            return criteriaBuilder.equal(root.get("deleted"), deleted);
        };
    }

    /**
     * Filtra por lista de centros médicos
     */
    public static Specification<MedicalConsultation> centerIdIn(List<Long> centerIds) {
        return (root, query, criteriaBuilder) -> {
            if (centerIds == null || centerIds.isEmpty()) {
                return null; // No aplica filtro
            }
            return root.get("centerId").in(centerIds);
        };
    }

    /**
     * Filtra por lista de médicos
     */
    public static Specification<MedicalConsultation> doctorIdIn(List<Long> doctorIds) {
        return (root, query, criteriaBuilder) -> {
            if (doctorIds == null || doctorIds.isEmpty()) {
                return null; // No aplica filtro
            }
            return root.get("doctorId").in(doctorIds);
        };
    }

    /**
     * Combina todos los filtros
     */
    public static Specification<MedicalConsultation> withFilters(
            LocalDateTime dateStart,
            LocalDateTime dateEnd,
            Boolean deleted,
            List<Long> centerIds,
            List<Long> doctorIds) {
        
        return Specification.where(dateGreaterThanOrEqual(dateStart))
                .and(dateLessThanOrEqual(dateEnd))
                .and(hasDeletedStatus(deleted))
                .and(centerIdIn(centerIds))
                .and(doctorIdIn(doctorIds));
    }
}
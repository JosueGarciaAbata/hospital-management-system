package consulting_service.repositories;

import consulting_service.entities.MedicalConsultation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones CRUD de consultas médicas con soporte para filtros avanzados
 */
public interface MedicalConsultationsRepository extends JpaRepository<MedicalConsultation, Long>,
        JpaSpecificationExecutor<MedicalConsultation> {

    Page<MedicalConsultation> findByDoctorIdAndDeletedFalse(Long doctorId, Pageable pageable);

    Optional<MedicalConsultation> findByIdAndDeletedFalse(Long id);

    boolean existsByCenterIdAndDeletedFalse(Long centerId);

    boolean existsByDoctorIdAndDeletedFalse(Long doctorId);

    Page<MedicalConsultation> findByCenterIdAndDeletedFalse(Long centerId, Pageable pageable);

    Page<MedicalConsultation> findByDeletedFalse(Pageable pageable);

    Page<MedicalConsultation> findByDoctorIdInAndDeletedFalse(List<Long> doctorIds, Pageable pageable);

    /**
     * Búsqueda con filtros dinámicos
     */
    @Query("SELECT mc FROM MedicalConsultation mc WHERE " +
            "(:dateStart IS NULL OR mc.date >= :dateStart) AND " +
            "(:dateEnd IS NULL OR mc.date <= :dateEnd) AND " +
            "(:deleted IS NULL OR mc.deleted = :deleted) AND " +
            "(:centerId IS NULL OR mc.centerId = :centerId) AND " +
            "(:doctorId IS NULL OR mc.doctorId = :doctorId)")
    Page<MedicalConsultation> findByFilters(
            @Param("dateStart") LocalDateTime dateStart,
            @Param("dateEnd") LocalDateTime dateEnd,
            @Param("deleted") Boolean deleted,
            @Param("centerId") Long centerId,
            @Param("doctorId") Long doctorId,
            Pageable pageable);

    /**
     * Búsqueda con múltiples centros médicos y médicos - DEPRECATED: Use Specifications instead
     * @deprecated Use {@link #findAll(Specification, Pageable)} with MedicalConsultationSpecifications
     */
    @Deprecated
    @Query("SELECT mc FROM MedicalConsultation mc WHERE " +
            "(:dateStart IS NULL OR mc.date >= :dateStart) AND " +
            "(:dateEnd IS NULL OR mc.date <= :dateEnd) AND " +
            "(:deleted IS NULL OR mc.deleted = :deleted) AND " +
            "(:centerIds IS NULL OR mc.centerId IN :centerIds) AND " +
            "(:doctorIds IS NULL OR mc.doctorId IN :doctorIds)")
    Page<MedicalConsultation> findByAdvancedFiltersLegacy(
            @Param("dateStart") LocalDateTime dateStart,
            @Param("dateEnd") LocalDateTime dateEnd,
            @Param("deleted") Boolean deleted,
            @Param("centerIds") List<Long> centerIds,
            @Param("doctorIds") List<Long> doctorIds,
            Pageable pageable);


    /**
     * Cuenta el número total de consultas médicas que coinciden con los filtros
     */
    @Query("SELECT COUNT(mc) FROM MedicalConsultation mc WHERE " +
            "(:dateStart IS NULL OR mc.date >= :dateStart) AND " +
            "(:dateEnd IS NULL OR mc.date <= :dateEnd) AND " +
            "(:deleted IS NULL OR mc.deleted = :deleted) AND " +
            "(:centerIds IS NULL OR mc.centerId IN :centerIds) AND " +
            "(:doctorIds IS NULL OR mc.doctorId IN :doctorIds)")
    long countByAdvancedFilters(
            @Param("dateStart") LocalDateTime dateStart,
            @Param("dateEnd") LocalDateTime dateEnd,
            @Param("deleted") Boolean deleted,
            @Param("centerIds") List<Long> centerIds,
            @Param("doctorIds") List<Long> doctorIds);

    /**
     * Encuentra la primera fecha de consulta que coincide con los filtros
     */
    @Query("SELECT MIN(mc.date) FROM MedicalConsultation mc WHERE " +
            "(:dateStart IS NULL OR mc.date >= :dateStart) AND " +
            "(:dateEnd IS NULL OR mc.date <= :dateEnd) AND " +
            "(:deleted IS NULL OR mc.deleted = :deleted) AND " +
            "(:centerIds IS NULL OR mc.centerId IN :centerIds) AND " +
            "(:doctorIds IS NULL OR mc.doctorId IN :doctorIds)")
    LocalDateTime findFirstDateByAdvancedFilters(
            @Param("dateStart") LocalDateTime dateStart,
            @Param("dateEnd") LocalDateTime dateEnd,
            @Param("deleted") Boolean deleted,
            @Param("centerIds") List<Long> centerIds,
            @Param("doctorIds") List<Long> doctorIds);

    /**
     * Encuentra la última fecha de consulta que coincide con los filtros
     */
    @Query("SELECT MAX(mc.date) FROM MedicalConsultation mc WHERE " +
            "(:dateStart IS NULL OR mc.date >= :dateStart) AND " +
            "(:dateEnd IS NULL OR mc.date <= :dateEnd) AND " +
            "(:deleted IS NULL OR mc.deleted = :deleted) AND " +
            "(:centerIds IS NULL OR mc.centerId IN :centerIds) AND " +
            "(:doctorIds IS NULL OR mc.doctorId IN :doctorIds)")
    LocalDateTime findLastDateByAdvancedFilters(
            @Param("dateStart") LocalDateTime dateStart,
            @Param("dateEnd") LocalDateTime dateEnd,
            @Param("deleted") Boolean deleted,
            @Param("centerIds") List<Long> centerIds,
            @Param("doctorIds") List<Long> doctorIds);

    /**
     * Cuenta el número de centros médicos distintos
     */
    @Query("SELECT COUNT(DISTINCT mc.centerId) FROM MedicalConsultation mc WHERE " +
            "(:dateStart IS NULL OR mc.date >= :dateStart) AND " +
            "(:dateEnd IS NULL OR mc.date <= :dateEnd) AND " +
            "(:deleted IS NULL OR mc.deleted = :deleted) AND " +
            "(:centerIds IS NULL OR mc.centerId IN :centerIds) AND " +
            "(:doctorIds IS NULL OR mc.doctorId IN :doctorIds)")
    long countDistinctCentersIdByAdvancedFilters(
            @Param("dateStart") LocalDateTime dateStart,
            @Param("dateEnd") LocalDateTime dateEnd,
            @Param("deleted") Boolean deleted,
            @Param("centerIds") List<Long> centerIds,
            @Param("doctorIds") List<Long> doctorIds);

    /**
     * Cuenta el número de médicos distintos
     */
    @Query("SELECT COUNT(DISTINCT mc.doctorId) FROM MedicalConsultation mc WHERE " +
            "(:dateStart IS NULL OR mc.date >= :dateStart) AND " +
            "(:dateEnd IS NULL OR mc.date <= :dateEnd) AND " +
            "(:deleted IS NULL OR mc.deleted = :deleted) AND " +
            "(:centerIds IS NULL OR mc.centerId IN :centerIds) AND " +
            "(:doctorIds IS NULL OR mc.doctorId IN :doctorIds)")
    long countDistinctDoctorsIdByAdvancedFilters(
            @Param("dateStart") LocalDateTime dateStart,
            @Param("dateEnd") LocalDateTime dateEnd,
            @Param("deleted") Boolean deleted,
            @Param("centerIds") List<Long> centerIds,
            @Param("doctorIds") List<Long> doctorIds);

    /**
     * Cuenta el número de pacientes distintos
     */
    @Query("SELECT COUNT(DISTINCT mc.patientId) FROM MedicalConsultation mc WHERE " +
            "(:dateStart IS NULL OR mc.date >= :dateStart) AND " +
            "(:dateEnd IS NULL OR mc.date <= :dateEnd) AND " +
            "(:deleted IS NULL OR mc.deleted = :deleted) AND " +
            "(:centerIds IS NULL OR mc.centerId IN :centerIds) AND " +
            "(:doctorIds IS NULL OR mc.doctorId IN :doctorIds)")
    long countDistinctPatientsIdByAdvancedFilters(
            @Param("dateStart") LocalDateTime dateStart,
            @Param("dateEnd") LocalDateTime dateEnd,
            @Param("deleted") Boolean deleted,
            @Param("centerIds") List<Long> centerIds,
            @Param("doctorIds") List<Long> doctorIds);


    /**
     * Búsqueda con filtro por especialidades via JOIN con doctors (consulta nativa)
     */
    /**
     * Búsqueda con filtro por especialidades via JOIN con doctors (consulta nativa mejorada)
     */
    @Query(value = "SELECT mc.* FROM medical_consultation mc " +
            "JOIN doctor d ON mc.doctor_id = d.id " +
            "WHERE (COALESCE(:specialtyIds, NULL) IS NULL OR d.specialty_id IN (:specialtyIds)) " +
            "AND (:dateStart IS NULL OR mc.date >= :dateStart) " +
            "AND (:dateEnd IS NULL OR mc.date <= :dateEnd) " +
            "AND mc.deleted = false " +  // Siempre filtrar por deleted = false
            "AND (COALESCE(:centerIds, NULL) IS NULL OR mc.center_id IN (:centerIds)) " +
            "AND (COALESCE(:doctorIds, NULL) IS NULL OR mc.doctor_id IN (:doctorIds)) " +
            "ORDER BY mc.date DESC",
            countQuery = "SELECT COUNT(mc.id) FROM medical_consultation mc " +
                    "JOIN doctor d ON mc.doctor_id = d.id " +
                    "WHERE (COALESCE(:specialtyIds, NULL) IS NULL OR d.specialty_id IN (:specialtyIds)) " +
                    "AND (:dateStart IS NULL OR mc.date >= :dateStart) " +
                    "AND (:dateEnd IS NULL OR mc.date <= :dateEnd) " +
                    "AND mc.deleted = false " +
                    "AND (COALESCE(:centerIds, NULL) IS NULL OR mc.center_id IN (:centerIds)) " +
                    "AND (COALESCE(:doctorIds, NULL) IS NULL OR mc.doctor_id IN (:doctorIds))",
            nativeQuery = true)
    Page<MedicalConsultation> findBySpecialtiesAndFilters(
            @Param("specialtyIds") List<Long> specialtyIds,
            @Param("dateStart") LocalDateTime dateStart,
            @Param("dateEnd") LocalDateTime dateEnd,
            @Param("centerIds") List<Long> centerIds,
            @Param("doctorIds") List<Long> doctorIds,
            Pageable pageable);
}
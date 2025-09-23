package consulting_service.repositories;

import consulting_service.entities.MedicalConsultation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicalConsultationsRepository  extends JpaRepository<MedicalConsultation,Long> {

    Page<MedicalConsultation> findByDoctorIdAndDeletedFalse(Long doctorId, Pageable pageable);

    Optional<MedicalConsultation> findByIdAndDeletedFalse(Long id);

    boolean existsByCenterIdAndDeletedFalse(Long centerId);

    boolean existsByDoctorIdAndDeletedFalse(Long doctorId);

    Page<MedicalConsultation> findByCenterIdAndDeletedFalse(Long centerId, Pageable pageable);

    Page<MedicalConsultation> findByDeletedFalse(Pageable pageable);

    Page<MedicalConsultation> findByDoctorIdInAndDeletedFalse(List<Long> doctorIds, Pageable pageable);


}

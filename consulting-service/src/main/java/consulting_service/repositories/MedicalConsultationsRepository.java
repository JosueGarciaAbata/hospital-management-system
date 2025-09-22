package consulting_service.repositories;

import consulting_service.entities.MedicalConsultation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicalConsultationsRepository  extends JpaRepository<MedicalConsultation,Long> {

    List<MedicalConsultation> findByDoctorIdAndDeletedFalse(Long doctorId);

    Optional<MedicalConsultation> findByIdAndDeletedFalse(Long id);

    boolean existsByCenterIdAndDeletedFalse(Long centerId);

    boolean existsByDoctorIdAndDeletedFalse(Long doctorId);
}

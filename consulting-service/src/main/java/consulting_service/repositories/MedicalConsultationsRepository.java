package consulting_service.repositories;

import consulting_service.entities.MedicalConsultation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalConsultationsRepository  extends JpaRepository<MedicalConsultation,Long> {

    List<MedicalConsultation> findByDoctorId(Long doctorId);

}

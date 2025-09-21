package consulting_service.entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "medical_consultations")
public class MedicalConsultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotNull
    @Column(name = "patient_id", nullable = false)
    Long patientId;

    @NotNull
    @Column(name = "doctor_id", nullable = false)
    Long doctorId;

    @NotNull
    @Column(name = "center_id", nullable = false)
    Long centerId;

    @NotNull
    @Column(name = "date", nullable = false)
    LocalDateTime date;

    @NotBlank
    @Column(name = "diagnosis")
    String diagnosis;

    @NotBlank
    @Column(name = "treatment")
    String treatment;

    @NotBlank
    @Column(name = "notes")
    String notes;

    @Column(name = "deleted")
    Boolean deleted;

    @PrePersist
    public void prePersist() {

        this.deleted = false;
    }

}

package com.hospital.admin_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;

@Entity
@Table(name = "doctors")
@Getter @Setter
@SoftDelete(columnName = "deleted")
public class Doctor extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "specialty_id", foreignKey = @ForeignKey(name = "fk_doctor_specialty"))
    private Specialty specialty;
}

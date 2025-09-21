package com.hospital.admin_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;

@Entity
@Table(name = "medical_centers")
@Getter @Setter
@SoftDelete(columnName = "deleted")
public class MedicalCenter extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 100, nullable = false)
    private String city;

    @Column(length = 200, nullable = false)
    private String address;
}

package consulting_service.entities;

import consulting_service.enums.GenderType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Table(name="patients")
@Getter
@Setter
@Entity
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="dni", unique = true,length = 10)
    private String dni;

    @NotBlank
    @Column(name="first_name",length = 100)
    private String firstName;

    @NotBlank
    @Column(name="last_name",length = 100)
    private String lastName;

    @NotNull
    @Column(name="birth_date")
    private LocalDate birthDate;

    @NotNull
    @Enumerated( EnumType.STRING)
    private GenderType gender;

    @Column(name="center_id",nullable = false)
    private Long centerId;

    @Column(name="deleted")
    private Boolean deleted;

    @PrePersist
    public void prePersist() {
        this.deleted = false;
    }
}

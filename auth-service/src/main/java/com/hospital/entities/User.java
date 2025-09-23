package com.hospital.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hospital.enums.GenderType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "users")
@SoftDelete(strategy = SoftDeleteType.DELETED, columnName = "enabled")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "dni", unique = true)
    private String username;

    @NotBlank()
    private String password;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Enumerated(EnumType.STRING)
    private GenderType gender;

    @Column(nullable = false)
    private Long centerId;

    @ManyToMany()
    @JsonIgnoreProperties({ "users", "handler", "hibernateLazyInitializer" })
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            uniqueConstraints = { @UniqueConstraint(columnNames = { "user_id", "role_id" }) })
    private Set<Role> roles;

    public User() {
        this.roles = new HashSet<>();
    }
}

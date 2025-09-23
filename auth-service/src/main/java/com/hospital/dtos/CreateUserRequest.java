package com.hospital.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hospital.annotations.EcuadorianDni;
import com.hospital.enums.GenderType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class CreateUserRequest {

    @EcuadorianDni()
    @NotBlank(message = "El DNI no puede estar vacío")
    @Size(min = 3, max = 10, message = "El DNI debe tener entre 3 y 10 caracteres")
    private String username;

    @Email(message = "El correo electrónico no es válido")
    private String email;

    @NotBlank()
    @Size(min = 8,message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotNull(message = "El género no puede estar vacío")
    private GenderType gender;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @JsonProperty("first_name")
    private String firstName;

    @NotBlank(message = "El apellido no puede estar vacío")
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    @JsonProperty("last_name")
    private String lastName;

    @Positive(message = "El id del centro debe ser un número positivo")
    @NotNull(message = "El id del centro no puede estar vacío")
    @JsonProperty("center_id")
    private Long centerId;

    @NotNull(message = "El rol no puede estar vacío")
    private Set<RoleRequest> roles;

}

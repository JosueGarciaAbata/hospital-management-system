package com.hospital.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hospital.annotations.EcuadorianDni;
import com.hospital.enums.GenderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
@Schema(description = "Petición para registrar un nuevo usuario en el sistema")
public class CreateUserRequest {

    @Schema(description = "DNI del usuario (válido en Ecuador)", example = "1728394056")
    @EcuadorianDni()
    @NotBlank(message = "El DNI no puede estar vacío")
    @Size(min = 3, max = 10, message = "El DNI debe tener entre 3 y 10 caracteres")
    private String username;

    @Schema(description = "Correo electrónico del usuario", example = "usuario@correo.com")
    @Email(message = "El correo electrónico no es válido")
    private String email;

    @Schema(description = "Contraseña de acceso", example = "Password123!")
    @NotBlank()
    @Size(min = 8,message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @Schema(description = "Género del usuario", example = "MALE")
    @NotNull(message = "El género no puede estar vacío")
    private GenderType gender;

    @Schema(description = "Nombre del usuario", example = "Juan")
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @JsonProperty("first_name")
    private String firstName;

    @Schema(description = "Apellido del usuario", example = "Pérez")
    @NotBlank(message = "El apellido no puede estar vacío")
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    @JsonProperty("last_name")
    private String lastName;

    @Schema(description = "Identificador del centro médico asociado", example = "5")
    @Positive(message = "El id del centro debe ser un número positivo")
    @NotNull(message = "El id del centro no puede estar vacío")
    @JsonProperty("center_id")
    private Long centerId;

    @Schema(description = "Roles asignados al usuario")
    @NotNull(message = "El rol no puede estar vacío")
    private Set<RoleRequest> roles;
}

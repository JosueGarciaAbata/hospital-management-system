package com.hospital.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hospital.enums.GenderType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Respuesta que representa los datos de un usuario")
public class UserResponse {

    @Schema(description = "Identificador único del usuario", example = "25")
    private Long id;

    @Schema(description = "DNI del usuario", example = "1728394056")
    private String username;

    @Schema(description = "Correo electrónico del usuario", example = "usuario@correo.com")
    private String email;

    @Schema(description = "Género del usuario", example = "MALE")
    private GenderType gender;

    private boolean enabled;

    @Schema(description = "Nombre del usuario", example = "Luis")
    @JsonProperty("first_name")
    private String firstName;

    @Schema(description = "Apellido del usuario", example = "Torres")
    @JsonProperty("last_name")
    private String lastName;

    @Schema(description = "Identificador del centro médico asociado", example = "5")
    @JsonProperty("center_id")
    private Long centerId;

    @Schema(description = "Nombre del centro médico asociado", example = "Hospital Central")
    @JsonProperty("center_name")
    private String centerName;
}

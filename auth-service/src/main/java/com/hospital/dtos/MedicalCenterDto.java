package com.hospital.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "Información básica de un centro médico")
public class MedicalCenterDto {

    @Schema(description = "Identificador único del centro", example = "10")
    private Long id;

    @Schema(description = "Nombre del centro médico", example = "Clínica Central")
    private String name;

    @Schema(description = "Ciudad donde se ubica el centro médico", example = "Quito")
    private String city;

    @Schema(description = "Dirección exacta del centro", example = "Av. Amazonas N34-125")
    private String address;
}

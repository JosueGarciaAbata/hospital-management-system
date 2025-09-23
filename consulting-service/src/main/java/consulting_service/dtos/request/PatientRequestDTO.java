package consulting_service.dtos.request;

import consulting_service.annotations.EcuadorianDni;
import consulting_service.enums.GenderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.*;

public record PatientRequestDTO(

        @NotBlank(message="La cedula es obligatoria")
        @Size(min=10,max=10,message="La cedula debe tener 10 digitos")
        @EcuadorianDni
        String dni,

        @NotBlank(message="El nombre es obligatorio")
        String firstName,

        @NotBlank(message="El apellido es obligatorio")
        String lastName,

        @Past(message="Fecha de nacimiento inválida")
        LocalDate birthDate,

        @NotNull(message="El género es obligatorio")
        GenderType gender,


        @NotNull(message="El centro medico es obligatorio")
        Long centerId

) {
}

package consulting_service.mappers;

import consulting_service.dtos.request.PatientRequestDTO;
import consulting_service.dtos.response.PatientResponseDTO;
import consulting_service.entities.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")

public interface PatientMapper {

    PatientMapper mapper = Mappers.getMapper(PatientMapper.class);


    PatientResponseDTO toDTO(Patient patient);


    Patient toEntity(PatientRequestDTO dto);
}

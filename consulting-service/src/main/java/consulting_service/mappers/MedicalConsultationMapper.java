package consulting_service.mappers;

import consulting_service.dtos.request.MedicalConsultationRequestDTO;
import consulting_service.dtos.response.MedicalConsultations.MedicalConsultationResponseDTO;
import consulting_service.entities.MedicalConsultation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MedicalConsultationMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "date", target = "date")
    @Mapping(source = "diagnosis", target = "diagnosis")
    @Mapping(source = "treatment", target = "treatment")
    @Mapping(source = "notes", target = "notes")
    MedicalConsultationResponseDTO toDTO(MedicalConsultation entity);


    MedicalConsultation toEntity(MedicalConsultationRequestDTO request);

    void updateEntityFromDto(MedicalConsultationRequestDTO request, @MappingTarget MedicalConsultation entity);
}
package com.hospital.admin_service.mapper;

import com.hospital.admin_service.dto.medicalCenter.MedicalCenterCreateRequest;
import com.hospital.admin_service.dto.medicalCenter.MedicalCenterRead;
import com.hospital.admin_service.dto.medicalCenter.MedicalCenterUpdateRequest;
import com.hospital.admin_service.model.MedicalCenter;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MedicalCenterMapper {

    MedicalCenterRead toRead(MedicalCenter entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    MedicalCenter toEntity(MedicalCenterCreateRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntityFromDto(MedicalCenterUpdateRequest dto, @MappingTarget MedicalCenter entity);
}

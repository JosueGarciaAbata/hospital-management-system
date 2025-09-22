package com.hospital.admin_service.mapper;

import com.hospital.admin_service.dto.specialty.*;
import com.hospital.admin_service.model.Specialty;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SpecialtyMapper {

    @Mappings({
            @Mapping(target = "id",        source = "id"),
            @Mapping(target = "version",   source = "version"),
            @Mapping(target = "name",      source = "name"),
            @Mapping(target = "description", source = "description"),
            @Mapping(target = "createdAt", source = "createdAt"),
            @Mapping(target = "updatedAt", source = "updatedAt")
    })
    SpecialtyRead toRead(Specialty entity);

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "version",   ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Specialty toEntity(SpecialtyCreateRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "version",   ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(SpecialtyUpdateRequest dto, @MappingTarget Specialty entity);
}

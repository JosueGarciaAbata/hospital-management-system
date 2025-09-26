package com.hospital.admin_service.mapper;

import com.hospital.admin_service.dto.doctor.DoctorCreateRequest;
import com.hospital.admin_service.dto.doctor.DoctorRead;
import com.hospital.admin_service.dto.doctor.DoctorUpdateRequest;
import com.hospital.admin_service.model.Doctor;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DoctorMapper {

    /* ===== Entity -> Read DTO ===== */
    @Mappings({
            @Mapping(target = "id",            source = "id"),
            @Mapping(target = "version",       source = "version"),
            @Mapping(target = "userId",        source = "userId"),
            @Mapping(target = "specialtyId",   source = "specialty.id"),
            @Mapping(target = "specialtyName", source = "specialty.name"),
            // Estos 4 se enriquecen fuera del mapper (servicio/assembler)
            @Mapping(target = "username",  ignore = true),
            @Mapping(target = "firstName", ignore = true),
            @Mapping(target = "lastName",  ignore = true),
            @Mapping(target = "gender",    ignore = true),
            @Mapping(target = "createdAt",     source = "createdAt"),
            @Mapping(target = "updatedAt",     source = "updatedAt")
    })
    DoctorRead toRead(Doctor entity);

    /* ===== Create DTO -> Entity =====
       specialty se resuelve en el servicio con specialtyId (no aquí)
    */
    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "version",   ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "specialty", ignore = true) // se setea en el servicio
    @Mapping(target = "deleted", ignore = true)
    Doctor toEntity(DoctorCreateRequest dto);

    /* ===== Update DTO -> (patch) Entity =====
       Solo copiamos campos no-null; specialty se maneja en el servicio
    */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "version",   ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "specialty", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntityFromDto(DoctorUpdateRequest dto, @MappingTarget Doctor entity);
}

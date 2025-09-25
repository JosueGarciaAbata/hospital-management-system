package com.hospital.admin_service.service.doctor;

import com.hospital.admin_service.dto.doctor.DoctorRead;
import com.hospital.admin_service.external.dto.user.UserResponse;
import com.hospital.admin_service.external.port.IAuthUserClient;
import com.hospital.admin_service.mapper.DoctorMapper;
import com.hospital.admin_service.model.Doctor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Page;

@Component
@RequiredArgsConstructor
public class DoctorReadAssembler {

    private final DoctorMapper mapper;
    private final IAuthUserClient authClient;

    public DoctorRead toRead(Doctor entity) {
        DoctorRead base = mapper.toRead(entity);
        if (entity.getUserId() == null) return base;

        UserResponse user = authClient.getUserById(entity.getUserId());
        if (user == null) return base;
        return new DoctorRead(
                base.id(),
                base.version(),
                base.userId(),
                base.specialtyId(),
                base.specialtyName(),
                user.username(),
                user.firstName(),
                user.lastName(),
                user.gender(),
                base.createdAt(),
                base.updatedAt(),
                base.deleted()
        );
    }

    public Page<DoctorRead> toReadPage(Page<Doctor> page) {
        return page.map(this::toRead);
    }
}
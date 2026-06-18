package consulting_service.feign.admin_service.services;

import consulting_service.configs.FeignConfig;
import consulting_service.exceptions.NotFoundException;
import consulting_service.feign.admin_service.dtos.DoctorRead;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;


import java.util.List;


@FeignClient(name = "admin-service", contextId = "doctorServiceClient", configuration = FeignConfig.class)
public interface DoctorServiceClient {

    String ROLE = "ADMIN";

    @GetMapping("/admin/doctors/{id}")
    ResponseEntity<DoctorRead> getOne(
            @PathVariable("id") Long id,
            @RequestParam(name = "includeDeleted", defaultValue = "false") boolean includeDeleted,
            @RequestHeader("X-Roles") String roles
    );

    default Long getUserId(Long id) {
        ResponseEntity<DoctorRead> response = getOne(id, false, ROLE);
        DoctorRead doctor = response.getBody();
        if (doctor == null) throw new NotFoundException("Médico no encontrado");
        return doctor.userId();
    }

    @GetMapping("/admin/doctors/by-specialty/{specialtyId}")
    ResponseEntity<Page<DoctorRead>> getDoctorsBySpecialty(
            @PathVariable("specialtyId") Long specialtyId,
            Pageable pageable,
            @RequestHeader("X-Roles") String roles
    );

    default List<Long> getDoctorIdsBySpecialty(Long specialtyId, Pageable pageable) {
        ResponseEntity<Page<DoctorRead>> response = getDoctorsBySpecialty(specialtyId, pageable, ROLE);
        Page<DoctorRead> doctors = response.getBody();
        if (doctors == null || doctors.isEmpty()) throw new NotFoundException("No se encontraron doctores con esa especialidad");
        return doctors.stream().map(DoctorRead::id).toList();
    }
}
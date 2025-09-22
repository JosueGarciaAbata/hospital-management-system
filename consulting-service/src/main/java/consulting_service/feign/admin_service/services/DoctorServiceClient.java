package consulting_service.feign.admin_service.services;

import consulting_service.exceptions.NotFoundException;
import consulting_service.feign.admin_service.dtos.DoctorRead;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "admin-service", contextId = "doctorServiceClient")
public interface DoctorServiceClient {

    String ROLE= "ADMIN";

    @GetMapping("/admin/doctors/{id}")
    ResponseEntity<DoctorRead> getOne(
            @PathVariable("id") Long id,
            @RequestParam(name = "includeDeleted", defaultValue = "false") boolean includeDeleted,
            @RequestHeader("X-Roles") String roles
    );

    default Long getUserId(Long id) {
        ResponseEntity<DoctorRead> response = getOne(id, false, ROLE);
        DoctorRead mc = response.getBody();
        if(mc == null) throw new NotFoundException("Médico no encontrado");
        return mc.userId();
    }



}

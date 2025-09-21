package consulting_service.feign.admin_service.services;

import consulting_service.exceptions.NotFoundException;
import consulting_service.dtos.response.MedicalConsultations.MedicalCenterReadDTO;
import consulting_service.feign.admin_service.dtos.MedicalCenterRead;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "admin-service")
public interface MedicalCenterServiceClient {

    String ROLE= "ADMIN";

    @GetMapping("/admin/centers/{id}")
    ResponseEntity<MedicalCenterRead> getOne(
            @PathVariable("id") Long id,
            @RequestParam(name = "includeDeleted", defaultValue = "false") boolean includeDeleted,
            @RequestHeader("X-Roles") String roles
    );

    default MedicalCenterReadDTO getName(Long id) {
        ResponseEntity<MedicalCenterRead> response = getOne(id, false, ROLE);
        MedicalCenterRead mc = response.getBody();
        if(mc == null) throw new NotFoundException("Centro médico no encontrado");
        return new MedicalCenterReadDTO(mc.id(), mc.name());
    }
}

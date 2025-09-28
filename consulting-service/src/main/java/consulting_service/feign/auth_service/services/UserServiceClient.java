package consulting_service.feign.auth_service.services;

import consulting_service.configs.FeignConfig;
import consulting_service.dtos.response.MedicalConsultations.DoctorReadDTO;
import consulting_service.exceptions.NotFoundException;
import consulting_service.feign.auth_service.dtos.UserResponseSV;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth-service", configuration = FeignConfig.class)
public interface UserServiceClient {

    @GetMapping("/auth/users/{id}")
    ResponseEntity<UserResponseSV> findById(@PathVariable("id") Long userId, @RequestParam(defaultValue = "false") boolean enabled);

    default DoctorReadDTO getDoctorByUserId(Long id) {
        try {
            ResponseEntity<UserResponseSV> response = findById(id, false);
            UserResponseSV data = response.getBody();
            if (data == null) throw new NotFoundException("Usuario no encontrado");
            return new DoctorReadDTO(null, data.getFirstName(), data.getLastName());
        } catch (Exception e) {
            throw new NotFoundException("Usuario no encontrado: " + e.getMessage());
        }
    }

}
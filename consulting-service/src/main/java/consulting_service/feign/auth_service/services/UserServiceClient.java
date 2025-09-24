package consulting_service.feign.auth_service.services;

import consulting_service.dtos.response.MedicalConsultations.DoctorReadDTO;
import consulting_service.exceptions.NotFoundException;
import consulting_service.feign.auth_service.dtos.UserResponseSV;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service")
public interface UserServiceClient {

    @GetMapping("/auth/users/me/{id}")
    ResponseEntity<UserResponseSV> findById(@PathVariable("id") Long id);

    default DoctorReadDTO getDoctorByUserId(Long id) {
        ResponseEntity<UserResponseSV> response = findById(id);
        UserResponseSV data = response.getBody();
        if (data == null) throw new NotFoundException("Usuario no encontrado");
        return new DoctorReadDTO(null, data.getFirstName(), data.getLastName());
    }

}

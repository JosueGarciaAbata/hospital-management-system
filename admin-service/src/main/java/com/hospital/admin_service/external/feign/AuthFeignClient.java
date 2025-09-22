package com.hospital.admin_service.external.feign;

import com.hospital.admin_service.config.FeignConfig;
import com.hospital.admin_service.external.dto.user.CreateUserForDoctorRequest;
import com.hospital.admin_service.external.dto.user.UserResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "auth-service", path = "/auth", configuration = FeignConfig.class)
public interface AuthFeignClient {

    @PostMapping("/register")
    @CircuitBreaker(name = "authService")
    @Retry(name = "authService")
    ResponseEntity<UserResponse> register(@RequestBody CreateUserForDoctorRequest body);

    @GetMapping("/users/me/{id}")
    @CircuitBreaker(name = "authService")
    @Retry(name = "authService")
    ResponseEntity<UserResponse> getUserById(@PathVariable("id") Long id);

    @GetMapping("/users/by-center/{id}")
    @CircuitBreaker(name = "authService") @Retry(name = "authService")
    ResponseEntity<UserResponse> getUserByCenterId(
            @PathVariable("id") Long id,
            @RequestParam(name = "includeDisabled", defaultValue = "false") boolean includeDisabled
    );

    @RequestMapping(value = "/users/by-center/{id}/exists", method = RequestMethod.HEAD)
    @CircuitBreaker(name = "authService") @Retry(name = "authService")
    ResponseEntity<Void> existsUserByCenterId(
            @PathVariable("id") Long id,
            @RequestParam(name = "includeDisabled", defaultValue = "false") boolean includeDisabled
    );

    @DeleteMapping("/{id}")
    @CircuitBreaker(name = "authService")
    @Retry(name = "authService")
    ResponseEntity<Void> deleteUser(@PathVariable("id") Long id,
                                    @RequestParam("hard") boolean hard);

}

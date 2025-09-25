package com.hospital.rests;

import com.hospital.dtos.*;
import com.hospital.entities.User;
import com.hospital.exceptions.SelfDeletionNotAllowedException;
import com.hospital.mappers.UserMapper;
import com.hospital.security.aop.RequireRole;
import com.hospital.services.PasswordResetService;
import com.hospital.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class UserAndAuthController {

    private final UserService service;
    private final UserMapper mapper;
    private final PasswordResetService passwordResetService;

    @RequireRole("ADMIN")
    @GetMapping("/users/all-testing")
    public ResponseEntity<List<UserResponse>> findAllTesting(){
        return ResponseEntity.ok(service.findAllTesting().stream().map(mapper::toUserResponse).collect(Collectors.toList()));
    }

    @RequireRole("ADMIN")
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> findAllUsers(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "false") boolean includeDeleted) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return ResponseEntity.ok(service.findAllExludingUser(Long.parseLong(userId), pageable, includeDeleted));
    }

    @GetMapping("/users/me")
    public ResponseEntity<UserResponse> me(@RequestHeader("X-User-Id") String userId) {
        User user = service.findUserById(Long.parseLong(userId));
        return ResponseEntity.ok(mapper.toUserResponse(user));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = service.findUserById(id);
        return ResponseEntity.ok(mapper.toUserResponse(user));
    }

    @GetMapping("/users/by-center/{id}")
    public ResponseEntity<UserResponse> getUserByCenterId(
            @PathVariable Long id,
            @RequestParam(name = "includeDisabled", defaultValue = "false") boolean includeDisabled) {

        UserResponse response = mapper.toUserResponse(service.findUserByCenterId(id, includeDisabled));
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/users/by-center/{id}/exists", method = RequestMethod.HEAD)
    public ResponseEntity<Void> existsUserByCenter(
            @PathVariable Long id,
            @RequestParam(name = "includeDisabled", defaultValue = "false") boolean includeDisabled) {

        boolean exists = service.existsUserByCenterId(id, includeDisabled);
        return exists ? ResponseEntity.noContent().build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @RequireRole("ADMIN")
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid CreateUserRequest request) {
        UserResponse response = mapper.toUserResponse(this.service.register(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @RequireRole("ADMIN")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        UserResponse updatedUser = mapper.toUserResponse(this.service.update(id, request));
        return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
    }

    @RequireRole("ADMIN")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,
                                           @RequestParam(name = "hard", defaultValue = "false") boolean hard) {

        // Chequeando si el usuario es un doctor. Si es asi, no se puede eliminar
        this.service.validateDoctorAssigned(id);

        if (hard) {
            this.service.hardDeleteUser(id);
        } else {
            this.service.disableUser(id);
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/request-reset")
    public ResponseEntity<Void> requestReset(@RequestBody RequestPasswordRequest input) {
        passwordResetService.requestPasswordReset(input.getInput());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok().build();
    }
}

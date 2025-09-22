package com.hospital.rests;

import com.hospital.dtos.CreateUserRequest;
import com.hospital.dtos.UpdatePasswordRequest;
import com.hospital.dtos.UpdateUserRequest;
import com.hospital.dtos.UserResponse;
import com.hospital.entities.User;
import com.hospital.mappers.UserMapper;
import com.hospital.services.UserService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RateLimiter(name = "userService")
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class UserController {

    private final UserService service;
    private final UserMapper mapper;

    @GetMapping("/users/me/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        UserResponse response = mapper.toUserResponse(this.service.findUserById(id));
        return ResponseEntity.ok(response);
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

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid CreateUserRequest request) {
        UserResponse response = mapper.toUserResponse(this.service.register(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {

        User updatedUser = this.service.update(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(@PathVariable Long id, @RequestBody UpdatePasswordRequest request) {
        this.service.updatePassword(id, request);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,
                                           @RequestParam(name = "hard", defaultValue = "false") boolean hard) {
        if (hard) {
            this.service.hardDeleteUser(id);
        } else {
            this.service.disableUser(id);
        }
        return ResponseEntity.noContent().build();
    }
}

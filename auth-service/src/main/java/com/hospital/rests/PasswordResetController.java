package com.hospital.rests;

import com.hospital.dtos.RequestPasswordRequest;
import com.hospital.dtos.ResetPasswordRequest;
import com.hospital.services.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

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

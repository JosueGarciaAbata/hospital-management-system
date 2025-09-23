package com.hospital.services;

import com.hospital.dtos.ResetPasswordRequest;
import com.hospital.entities.VerificationToken;

public interface PasswordResetService {

    String createToken();
    VerificationToken generateForUser(Long userId);
    void markAsUsed(Long id);
    void requestPasswordReset(String input);
    void resetPassword(ResetPasswordRequest req);

}



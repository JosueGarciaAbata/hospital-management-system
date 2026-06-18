package com.hospital.services;

import com.hospital.dtos.ResetPasswordRequest;
import com.hospital.emails.EmailService;
import com.hospital.entities.VerificationToken;
import com.hospital.entities.User;
import com.hospital.exceptions.EmailTemplateException;
import com.hospital.exceptions.InvalidTokenException;
import com.hospital.exceptions.TokenNotFoundException;
import com.hospital.exceptions.UserNotFoundException;
import com.hospital.repositories.VerificationTokenRepository;
import com.hospital.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DefaultPasswordResetService implements PasswordResetService {

    private final UserService userService;
    @Value("${FRONTEND_URL}")
    private String frontendUrl;

    private final VerificationTokenRepository repository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    public String createToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    public VerificationToken generateForUser(Long userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setUser(user);
        verificationToken.setToken(UUID.randomUUID().toString());

        int expirationTime = 10;
        verificationToken.setExpiration(LocalDateTime.now().plusMinutes(expirationTime));
        return repository.save(verificationToken);
    }

    @Override
    public void markAsUsed(Long id) {
        VerificationToken existingToken = repository.findById(id).orElseThrow(() -> new TokenNotFoundException("No se encontro el token con id: " + id));
        existingToken.setUsed(true);
        repository.save(existingToken);
    }

    @Transactional
    @Override
    public void requestPasswordReset(String input) {

        User user = userRepository.findByUsernameOrEmail(input).orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + input));
        VerificationToken token = this.generateForUser(user.getId());

        String resetUrl = this.frontendUrl + "/reset?token=" + token.getToken();

        try {
            ClassPathResource resource = new ClassPathResource("templates/emails/password-reset.html");
            String html = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            html = html.replace("${user}", user.getFirstName() + " " + user.getLastName());
            html = html.replace("${reset_url}", resetUrl);

            emailService.sendEmail(user.getEmail(), "Restablece tu contraseña", html);

        } catch (IOException e) {
            throw new EmailTemplateException("Error al leer la plantilla de email: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void resetPassword(ResetPasswordRequest req) {

        VerificationToken token = repository.findByToken(req.getToken()).orElseThrow(() -> new TokenNotFoundException("Token no encontrado"));

        if (token.getExpiration().isBefore(LocalDateTime.now()) || Boolean.TRUE.equals(token.getUsed())) {
            throw new InvalidTokenException("El token expiró o ya fue usado");
        }

        this.markAsUsed(token.getId());
        User user = token.getUser();
        userService.updatePassword(user.getId(), req.getNewPassword());
    }
}

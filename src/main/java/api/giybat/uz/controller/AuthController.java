package api.giybat.uz.controller;

import api.giybat.uz.dto.AppResponse;
import api.giybat.uz.dto.auth.AuthDTO;
import api.giybat.uz.dto.ProfileDTO;
import api.giybat.uz.dto.auth.RegistrationDTO;
import api.giybat.uz.dto.auth.ResetPasswordConfirmDTO;
import api.giybat.uz.dto.auth.ResetPasswordDTO;
import api.giybat.uz.dto.sms.SmsResendDTO;
import api.giybat.uz.dto.sms.SmsVerificationDTO;
import api.giybat.uz.enums.AppLanguage;
import api.giybat.uz.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "AuthController", description = "Controller for Authentication and authorization")
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registration")
    @Operation(summary = "Profile registration", description = "Api used for registration")
    public ResponseEntity<AppResponse<String>> registration(
            @Valid @RequestBody RegistrationDTO dto,
            @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang) {
        log.info("Registration: name: {}, username: {}", dto.getName(), dto.getUsername());
        return ResponseEntity.ok().body(authService.registration(dto, lang));
    }

    @GetMapping("/registration/email-verification/{token}")
    @Operation(summary = "Email verification", description = "Api used for registration verification using email")
    public ResponseEntity<AppResponse<String>> emailVerification(
            @PathVariable("token") String token,
            @RequestParam(value = "lang", defaultValue = "UZ") AppLanguage lang) {
        log.info("Registration email verification: token: {}", token);
        return ResponseEntity.ok().body(authService.registrationEmailVerification(token, lang));
    }

    @PostMapping("/registration/sms-verification")
    @Operation(summary = "Sms verification", description = "Api used for registration verification using sms")
    public ResponseEntity<ProfileDTO> smsVerification(
            @Valid @RequestBody SmsVerificationDTO dto,
            @RequestParam(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang) {
        log.info("Sms verification: {}", dto);
        return ResponseEntity.ok().body(authService.registrationSmsVerification(dto, lang));
    }

    @PostMapping("/registration/sms-verification-resend")
    @Operation(summary = "Sms verification resend", description = "Api used for resent sms verification code")
    public ResponseEntity<AppResponse<String>> smsVerificationResend(
            @Valid @RequestBody SmsResendDTO dto,
            @RequestParam(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang) {
        log.info("Sms verification resent: {}", dto.getPhone());
        return ResponseEntity.ok().body(authService.registrationSmsVerificationResend(dto, lang));
    }

    @PostMapping("/login")
    @Operation(summary = "Login (Auth) api", description = "Api used for log-in to system")
    public ResponseEntity<ProfileDTO> login(
            @Valid @RequestBody AuthDTO dto,
            @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang) {
        log.info("Login: {}", dto.getUsername());
        return ResponseEntity.ok().body(authService.login(dto, lang));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset Password", description = "Api used for password reset")
    public ResponseEntity<AppResponse<String>> resetPassword (
            @Valid @RequestBody ResetPasswordDTO dto,
            @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang) {
        log.info("Reset Password: {}", dto.getUsername());
        return ResponseEntity.ok().body(authService.resetPassword(dto, lang));
    }

    @PostMapping("/reset-password-confirm")
    @Operation(summary = "Reset Password Confirm", description = "Api used for password reset confirm")
    public ResponseEntity<AppResponse<String>> resetPassword (
            @Valid @RequestBody ResetPasswordConfirmDTO dto,
            @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang) {
        log.info("Reset Password Confirm: {}", dto.getUsername());
        return ResponseEntity.ok().body(authService.resetPasswordConfirm(dto, lang));
    }
}

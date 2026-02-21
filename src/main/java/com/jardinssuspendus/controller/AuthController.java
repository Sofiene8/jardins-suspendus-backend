package com.jardinssuspendus.controller;

import com.jardinssuspendus.dto.request.LoginRequest;
import com.jardinssuspendus.dto.request.OTPVerificationRequest;
import com.jardinssuspendus.dto.request.RegisterRequest;
import com.jardinssuspendus.dto.response.ApiResponse;
import com.jardinssuspendus.dto.response.AuthResponse;
import com.jardinssuspendus.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /** Inscription : crée un compte désactivé et envoie un OTP */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Inscription réussie. Un code de vérification a été envoyé à " + request.getEmail()
                ));
    }

    /** Activation du compte via le code OTP reçu par email */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOTP(
            @Valid @RequestBody OTPVerificationRequest request
    ) {
        AuthResponse auth = authService.verifyOTPAndActivate(request.getEmail(), request.getCode());
        return ResponseEntity.ok(ApiResponse.success("Compte activé avec succès !", auth));
    }

    /** Renvoi d'un nouveau code OTP */
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOTP(@RequestParam String email) {
        authService.resendOTP(email);
        return ResponseEntity.ok(ApiResponse.success(
                "Un nouveau code de vérification a été envoyé à " + email
        ));
    }

    /** Connexion : retourne un token JWT */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse auth = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Connexion réussie", auth));
    }
}
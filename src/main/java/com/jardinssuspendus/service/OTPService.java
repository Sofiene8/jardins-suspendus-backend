package com.jardinssuspendus.service;

import com.jardinssuspendus.entity.OTP;
import com.jardinssuspendus.exception.BadRequestException;
import com.jardinssuspendus.repository.OTPRepository;
import com.jardinssuspendus.util.OTPGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OTPService {

    @Autowired
    private OTPRepository otpRepository;

    @Autowired
    private EmailService emailService;

    @Value("${otp.expiration}")
    private long otpExpiration; // en millisecondes

    @Value("${otp.length}")
    private int otpLength;

    @Transactional
    public void generateAndSendOTP(String email) {
        // Supprimer l'ancien OTP s'il existe
        otpRepository.deleteByEmail(email);

        // Générer nouveau code
        String code = OTPGenerator.generateOTP(otpLength);

        // Calculer la date d'expiration
        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(otpExpiration / 1000);

        // Sauvegarder
        OTP otp = new OTP(email, code, expiryDate);
        otpRepository.save(otp);

        // Envoyer par email
        emailService.sendOTPEmail(email, code);
    }

    @Transactional
    public boolean verifyOTP(String email, String code) {
        OTP otp = otpRepository.findValidOTPByEmail(email, LocalDateTime.now())
                .orElseThrow(() -> new BadRequestException("Code OTP invalide ou expiré"));

        if (!otp.getCode().equals(code)) {
            throw new BadRequestException("Code OTP incorrect");
        }

        // Marquer comme utilisé
        otp.setUsed(true);
        otpRepository.save(otp);

        return true;
    }

    @Transactional
    public void cleanupExpiredOTPs() {
        otpRepository.deleteExpiredOTPs(LocalDateTime.now());
    }
}
package com.jardinssuspendus.util;

import java.security.SecureRandom;

public class OTPGenerator {

    private static final SecureRandom random = new SecureRandom();

    /**
     * Génère un code OTP numérique de la longueur spécifiée
     *
     * @param length Longueur du code OTP (généralement 6)
     * @return Code OTP
     */
    public static String generateOTP(int length) {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            otp.append(random.nextInt(10)); // Chiffres de 0 à 9
        }
        return otp.toString();
    }

    /**
     * Génère un code OTP de 6 chiffres par défaut
     */
    public static String generateOTP() {
        return generateOTP(6);
    }

    /**
     * Génère un code OTP alphanumérique
     */
    public static String generateAlphanumericOTP(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            otp.append(characters.charAt(random.nextInt(characters.length())));
        }
        return otp.toString();
    }
}
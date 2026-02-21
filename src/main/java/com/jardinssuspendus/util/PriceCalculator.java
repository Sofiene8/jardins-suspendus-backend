package com.jardinssuspendus.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class PriceCalculator {

    // Prix fixe pour les enfants de 6-12 ans (en DT)
    private static final BigDecimal CHILD_6_12_PRICE = new BigDecimal("50.00");

    // Les enfants de 0-5 ans sont gratuits

    /**
     * Calcule le prix total d'une réservation
     *
     * @param roomPricePerNight Prix de la chambre par nuit
     * @param startDate Date d'arrivée
     * @param endDate Date de départ
     * @param adults Nombre d'adultes
     * @param children6To12 Nombre d'enfants 6-12 ans
     * @param children0To5 Nombre d'enfants 0-5 ans
     * @return Prix total
     */
    public static BigDecimal calculateTotalPrice(
            BigDecimal roomPricePerNight,
            LocalDate startDate,
            LocalDate endDate,
            int adults,
            int children6To12,
            int children0To5) {

        // Calculer le nombre de nuits
        long nights = ChronoUnit.DAYS.between(startDate, endDate);
        if (nights <= 0) {
            throw new IllegalArgumentException("La date de départ doit être après la date d'arrivée");
        }

        // Prix pour les adultes (prix de la chambre × nombre de nuits × nombre d'adultes)
        BigDecimal adultPrice = roomPricePerNight
                .multiply(BigDecimal.valueOf(nights))
                .multiply(BigDecimal.valueOf(adults));

        // Prix pour les enfants 6-12 ans (prix fixe × nombre de nuits × nombre d'enfants)
        BigDecimal children6To12Price = CHILD_6_12_PRICE
                .multiply(BigDecimal.valueOf(nights))
                .multiply(BigDecimal.valueOf(children6To12));

        // Les enfants 0-5 ans sont gratuits
        BigDecimal children0To5Price = BigDecimal.ZERO;

        // Total
        return adultPrice.add(children6To12Price).add(children0To5Price);
    }

    /**
     * Calcule le nombre de nuits
     */
    public static long calculateNights(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Calcule le prix pour une seule nuit
     */
    public static BigDecimal calculatePricePerNight(
            BigDecimal roomPricePerNight,
            int adults,
            int children6To12) {
        
        BigDecimal adultPrice = roomPricePerNight.multiply(BigDecimal.valueOf(adults));
        BigDecimal childrenPrice = CHILD_6_12_PRICE.multiply(BigDecimal.valueOf(children6To12));
        
        return adultPrice.add(childrenPrice);
    }
}
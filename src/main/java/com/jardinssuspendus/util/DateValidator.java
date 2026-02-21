package com.jardinssuspendus.util;

import com.jardinssuspendus.exception.BadRequestException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DateValidator {

    public static void validateReservationDates(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();

        // La date d'arrivée peut être aujourd'hui ou dans le futur
        if (startDate.isBefore(today)) {
            throw new BadRequestException("La date d'arrivée ne peut pas être dans le passé");
        }

        // La date de départ doit être strictement après la date d'arrivée
        if (!endDate.isAfter(startDate)) {
            throw new BadRequestException("La date de départ doit être après la date d'arrivée");
        }

        // Minimum 1 nuit
        if (ChronoUnit.DAYS.between(startDate, endDate) < 1) {
            throw new BadRequestException("La réservation doit être d'au moins 1 nuit");
        }

        // Maximum 30 nuits
        if (ChronoUnit.DAYS.between(startDate, endDate) > 30) {
            throw new BadRequestException("La durée maximale de réservation est de 30 nuits");
        }
    }

    public static boolean canModifyReservation(LocalDate endDate) {
        return endDate.isAfter(LocalDate.now().plusDays(2));
    }

    public static boolean datesOverlap(
            LocalDate start1, LocalDate end1,
            LocalDate start2, LocalDate end2) {
        return !end1.isBefore(start2) && !start1.isAfter(end2);
    }
}
package com.jardinssuspendus.entity.enums;

public enum ReservationStatus {
    EN_ATTENTE,    // En attente de paiement
    EN_COURS,      // Paiement en cours
    VALIDEE,       // Réservation validée (payée)
    ANNULEE        // Annulée par client ou admin
}
package com.jardinssuspendus.entity.enums;

public enum PaymentStatus {
    EN_ATTENTE,    // En attente de paiement
    PAYE,          // Paiement réussi
    ECHOUE,        // Paiement échoué
    REMBOURSE      // Remboursé (en cas d'annulation)
}
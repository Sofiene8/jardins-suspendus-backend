package com.jardinssuspendus.controller;

import com.jardinssuspendus.dto.request.PaymentRequest;
import com.jardinssuspendus.dto.response.ApiResponse;
import com.jardinssuspendus.dto.response.PaymentResponse;
import com.jardinssuspendus.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // ─── Client ───────────────────────────────────────────────────────────────

    /**
     * Étape 1 : Enregistre la commande PayPal créée côté front
     * Le frontend crée d'abord la commande via PayPal SDK, puis appelle cet endpoint
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @Valid @RequestBody PaymentRequest request
    ) {
        PaymentResponse payment = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Paiement initié", payment));
    }

    /**
     * Étape 2 : Capture le paiement après approbation du client sur PayPal
     * Déclenche automatiquement l'email de confirmation
     */
    @PostMapping("/capture")
    public ResponseEntity<ApiResponse<PaymentResponse>> capturePayment(
            @RequestParam String paypalOrderId
    ) {
        PaymentResponse payment = paymentService.capturePayment(paypalOrderId);
        return ResponseEntity.ok(ApiResponse.success("Paiement réussi ! Votre réservation est confirmée.", payment));
    }

    /**
     * Étape 2 (échec) : Le client a annulé ou le paiement a échoué sur PayPal
     */
    @PostMapping("/fail")
    public ResponseEntity<ApiResponse<Void>> failPayment(
            @RequestParam String paypalOrderId,
            @RequestParam(required = false) String errorMessage
    ) {
        paymentService.failPayment(paypalOrderId, errorMessage);
        return ResponseEntity.ok(ApiResponse.success("Statut de paiement mis à jour"));
    }

    /** Récupérer le paiement d'une réservation */
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByReservationId(
            @PathVariable Long reservationId
    ) {
        PaymentResponse payment = paymentService.getPaymentByReservationId(reservationId);
        return ResponseEntity.ok(ApiResponse.success("Paiement récupéré", payment));
    }

    // ─── Admin ────────────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAllPayments() {
        return ResponseEntity.ok(ApiResponse.success("Paiements récupérés", paymentService.getAllPayments()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Paiement récupéré", paymentService.getPaymentById(id)));
    }
}
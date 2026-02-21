package com.jardinssuspendus.service;

import com.jardinssuspendus.dto.request.PaymentRequest;
import com.jardinssuspendus.dto.response.PaymentResponse;
import com.jardinssuspendus.entity.Payment;
import com.jardinssuspendus.entity.Reservation;
import com.jardinssuspendus.entity.enums.PaymentStatus;
import com.jardinssuspendus.entity.enums.ReservationStatus;
import com.jardinssuspendus.exception.BadRequestException;
import com.jardinssuspendus.exception.ResourceNotFoundException;
import com.jardinssuspendus.repository.PaymentRepository;
import com.jardinssuspendus.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired private PaymentRepository     paymentRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private EmailService          emailService;

    // ── Initialisation du paiement ────────────────────────────────────────────

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        // JOIN FETCH pour avoir la réservation complète
        Reservation reservation = reservationRepository
                .findByIdWithDetails(request.getReservationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Réservation", "id", request.getReservationId()));

        // Vérifier paiement existant
        paymentRepository.findByReservationId(reservation.getId()).ifPresent(p -> {
            if (p.getStatus() == PaymentStatus.PAYE)
                throw new BadRequestException("Cette réservation a déjà été payée");
        });

        if (reservation.getStatus() == ReservationStatus.ANNULEE)
            throw new BadRequestException("Impossible de payer une réservation annulée");

        Payment payment = new Payment();
        payment.setReservation(reservation);
        payment.setPaypalOrderId(request.getPaypalOrderId());
        payment.setPaypalPayerId(request.getPaypalPayerId());
        payment.setAmount(reservation.getTotalPrice());   // montant en DT
        payment.setCurrency(request.getCurrency() != null ? request.getCurrency() : "TND");
        payment.setStatus(PaymentStatus.EN_ATTENTE);

        Payment saved = paymentRepository.save(payment);

        // Passer la réservation en EN_COURS
        reservation.setStatus(ReservationStatus.EN_COURS);
        reservationRepository.save(reservation);

        return PaymentResponse.fromEntity(saved);
    }

    // ── Capture (confirmation PayPal) ─────────────────────────────────────────

    @Transactional
    public PaymentResponse capturePayment(String paypalOrderId) {
        // findByPaypalOrderIdWithReservation charge la réservation en JOIN FETCH
        Payment payment = paymentRepository.findByPaypalOrderIdWithReservation(paypalOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Paiement", "paypalOrderId", paypalOrderId));

        if (payment.getStatus() == PaymentStatus.PAYE)
            throw new BadRequestException("Ce paiement a déjà été capturé");

        // Marquer comme payé avec la date
        payment.setStatus(PaymentStatus.PAYE);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);

        // Charger la réservation complète pour éviter le lazy
        Reservation reservation = reservationRepository
                .findByIdWithDetails(payment.getReservation().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Réservation", "id", payment.getReservation().getId()));

        reservation.setStatus(ReservationStatus.VALIDEE);
        reservationRepository.save(reservation);

        // Email de confirmation (non bloquant)
        try { emailService.sendReservationConfirmationEmail(reservation); }
        catch (Exception e) { System.err.println("Email erreur: " + e.getMessage()); }

        return PaymentResponse.fromEntity(payment);
    }

    // ── Échec ─────────────────────────────────────────────────────────────────

    @Transactional
    public void failPayment(String paypalOrderId, String errorMessage) {
        Payment payment = paymentRepository.findByPaypalOrderIdWithReservation(paypalOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Paiement", "paypalOrderId", paypalOrderId));

        payment.setStatus(PaymentStatus.ECHOUE);
        payment.setErrorMessage(errorMessage);
        paymentRepository.save(payment);

        Reservation reservation = reservationRepository
                .findByIdWithDetails(payment.getReservation().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Réservation", "id", payment.getReservation().getId()));
        reservation.setStatus(ReservationStatus.EN_ATTENTE);
        reservationRepository.save(reservation);
    }

    // ── Lecture ───────────────────────────────────────────────────────────────

    public PaymentResponse getPaymentByReservationId(Long reservationId) {
        return PaymentResponse.fromEntity(
            paymentRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Paiement", "reservationId", reservationId)));
    }

    public PaymentResponse getPaymentById(Long id) {
        return PaymentResponse.fromEntity(
            paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement", "id", id)));
    }

    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
package com.jardinssuspendus.controller;

import com.jardinssuspendus.dto.request.ReservationRequest;
import com.jardinssuspendus.dto.response.ApiResponse;
import com.jardinssuspendus.dto.response.ReservationResponse;
import com.jardinssuspendus.entity.enums.ReservationStatus;
import com.jardinssuspendus.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    // ─── Client ───────────────────────────────────────────────────────────────

    /** Mes réservations */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getMyReservations() {
        return ResponseEntity.ok(ApiResponse.success(
                "Vos réservations", reservationService.getMyReservations()
        ));
    }

    /** Détail d'une réservation (client propriétaire ou admin) */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Réservation récupérée", reservationService.getReservationById(id)
        ));
    }

    /** Créer une réservation */
    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @Valid @RequestBody ReservationRequest request
    ) {
        ReservationResponse reservation = reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Réservation créée avec succès", reservation));
    }

    /** Modifier une réservation (si > 48h avant départ et statut EN_ATTENTE) */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponse>> updateReservation(
            @PathVariable Long id, @Valid @RequestBody ReservationRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Réservation mise à jour", reservationService.updateReservation(id, request)
        ));
    }

    /** Annuler une réservation (si > 48h avant départ) */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.ok(ApiResponse.success("Réservation annulée"));
    }

    // ─── Admin ────────────────────────────────────────────────────────────────

    /** Toutes les réservations */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getAllReservations(
            @RequestParam(required = false) ReservationStatus status
    ) {
        List<ReservationResponse> list = (status != null)
                ? reservationService.getReservationsByStatus(status)
                : reservationService.getAllReservations();
        return ResponseEntity.ok(ApiResponse.success("Réservations récupérées", list));
    }

    /** Valider une réservation manuellement */
    @PostMapping("/{id}/validate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> validateReservation(@PathVariable Long id) {
        reservationService.validateReservation(id);
        return ResponseEntity.ok(ApiResponse.success("Réservation validée"));
    }

    /** Reporter une réservation */
    @PatchMapping("/{id}/report")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReservationResponse>> reportReservation(
            @PathVariable Long id, @Valid @RequestBody ReservationRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Réservation reportée", reservationService.reportReservation(id, request)
        ));
    }
}
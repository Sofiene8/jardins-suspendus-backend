package com.jardinssuspendus.controller;

import com.jardinssuspendus.dto.request.FeedbackRequest;
import com.jardinssuspendus.dto.response.ApiResponse;
import com.jardinssuspendus.dto.response.FeedbackResponse;
import com.jardinssuspendus.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    // ─── Lecture (public) ─────────────────────────────────────────────────────

    @GetMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getFeedbacksByRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Avis de la chambre récupérés", feedbackService.getFeedbacksByRoomId(roomId)
        ));
    }

    // ─── Client authentifié ───────────────────────────────────────────────────

    /** Mes avis */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getMyFeedbacks() {
        return ResponseEntity.ok(ApiResponse.success("Vos avis", feedbackService.getMyFeedbacks()));
    }

    /** Soumettre un avis (nécessite une réservation validée) */
    @PostMapping
    public ResponseEntity<ApiResponse<FeedbackResponse>> createFeedback(
            @Valid @RequestBody FeedbackRequest request
    ) {
        FeedbackResponse feedback = feedbackService.createFeedback(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Avis soumis avec succès", feedback));
    }

    // ─── Admin ────────────────────────────────────────────────────────────────

    /** Tous les avis */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getAllFeedbacks() {
        return ResponseEntity.ok(ApiResponse.success("Tous les avis", feedbackService.getAllFeedbacks()));
    }

    /** Avis sans réponse */
    @GetMapping("/unanswered")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getUnansweredFeedbacks() {
        return ResponseEntity.ok(ApiResponse.success(
                "Avis en attente de réponse", feedbackService.getUnansweredFeedbacks()
        ));
    }

    /** Avis déjà répondus */
    @GetMapping("/answered")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getAnsweredFeedbacks() {
        return ResponseEntity.ok(ApiResponse.success(
                "Avis répondus", feedbackService.getAnsweredFeedbacks()
        ));
    }

    /** Répondre officiellement à un avis */
    @PostMapping("/{id}/respond")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FeedbackResponse>> respondToFeedback(
            @PathVariable Long id,
            @RequestParam String response
    ) {
        FeedbackResponse feedback = feedbackService.respondToFeedback(id, response);
        return ResponseEntity.ok(ApiResponse.success("Réponse publiée", feedback));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFeedback(@PathVariable Long id) {
        feedbackService.deleteFeedback(id);
        return ResponseEntity.ok(ApiResponse.success("Avis supprimé"));
    }
}
package com.jardinssuspendus.service;

import com.jardinssuspendus.dto.request.FeedbackRequest;
import com.jardinssuspendus.dto.response.FeedbackResponse;
import com.jardinssuspendus.entity.Feedback;
import com.jardinssuspendus.entity.Reservation;
import com.jardinssuspendus.entity.Room;
import com.jardinssuspendus.entity.User;
import com.jardinssuspendus.entity.enums.ReservationStatus;
import com.jardinssuspendus.exception.BadRequestException;
import com.jardinssuspendus.exception.ResourceNotFoundException;
import com.jardinssuspendus.repository.FeedbackRepository;
import com.jardinssuspendus.repository.ReservationRepository;
import com.jardinssuspendus.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedbackService {

    @Autowired private FeedbackRepository    feedbackRepository;
    @Autowired private RoomRepository        roomRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private UserService           userService;

    // ── Lecture ───────────────────────────────────────────────────────────────

    public List<FeedbackResponse> getAllFeedbacks() {
        return feedbackRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<FeedbackResponse> getFeedbacksByRoomId(Long roomId) {
        return feedbackRepository.findByRoomIdOrderByCreatedAtDesc(roomId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<FeedbackResponse> getMyFeedbacks() {
        User currentUser = userService.getCurrentUser();
        return feedbackRepository.findByUserId(currentUser.getId()).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<FeedbackResponse> getUnansweredFeedbacks() {
        return feedbackRepository.findUnansweredFeedbacks().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<FeedbackResponse> getAnsweredFeedbacks() {
        return feedbackRepository.findAnsweredFeedbacks().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    // ── Création ──────────────────────────────────────────────────────────────

    @Transactional
    public FeedbackResponse createFeedback(FeedbackRequest request) {
        User currentUser = userService.getCurrentUser();

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Chambre", "id", request.getRoomId()));

        Reservation reservation = null;

        if (request.getReservationId() != null) {
            // findByIdWithDetails charge user + room en JOIN FETCH
            reservation = reservationRepository.findByIdWithDetails(request.getReservationId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Réservation", "id", request.getReservationId()));

            if (!reservation.getUser().getId().equals(currentUser.getId())) {
                throw new BadRequestException("Cette réservation ne vous appartient pas");
            }

            if (reservation.getStatus() != ReservationStatus.VALIDEE
                    && reservation.getStatus() != ReservationStatus.EN_COURS) {
                throw new BadRequestException(
                        "Vous pouvez uniquement évaluer les réservations confirmées ou en cours");
            }

            if (feedbackRepository.findByReservationId(request.getReservationId()).isPresent()) {
                throw new BadRequestException("Vous avez déjà laissé un avis pour cette réservation");
            }
        }

        Feedback feedback = new Feedback();
        feedback.setUser(currentUser);
        feedback.setRoom(room);
        feedback.setReservation(reservation);
        feedback.setRating(request.getRating());
        feedback.setComment(request.getComment());

        Feedback saved = feedbackRepository.save(feedback);

        // Construire la réponse manuellement sans passer par fromEntity
        // (pour éviter tout problème de relation lazy après le save)
        return buildResponse(saved, currentUser, room, reservation);
    }

    // ── Réponse admin ─────────────────────────────────────────────────────────

    @Transactional
    public FeedbackResponse respondToFeedback(Long feedbackId, String response) {
        if (response == null || response.isBlank()) {
            throw new BadRequestException("La réponse ne peut pas être vide");
        }
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Avis", "id", feedbackId));
        feedback.setResponse(response.trim());
        feedback.setResponseDate(LocalDateTime.now());
        Feedback saved = feedbackRepository.save(feedback);
        return toResponse(saved);
    }

    // ── Suppression ───────────────────────────────────────────────────────────

    @Transactional
    public void deleteFeedback(Long id) {
        feedbackRepository.delete(
            feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Avis", "id", id))
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Construit FeedbackResponse depuis les objets déjà chargés — 0 accès lazy */
    private FeedbackResponse buildResponse(Feedback f, User user, Room room, Reservation reservation) {
        return new FeedbackResponse(
            f.getId(),
            user.getId(),
            user.getName(),
            room.getId(),
            room.getTitle(),
            reservation != null ? reservation.getId() : null,
            f.getRating(),
            f.getComment(),
            f.getResponse(),
            f.getResponseDate(),
            f.getCreatedAt()
        );
    }

    /** Pour les requêtes avec JOIN FETCH (findAll, findByUserId, etc.) */
    private FeedbackResponse toResponse(Feedback f) {
        try {
            return FeedbackResponse.fromEntity(f);
        } catch (Exception e) {
            // Fallback si les relations ne sont pas chargées
            System.err.println("Warning toResponse: " + e.getMessage());
            return new FeedbackResponse(
                f.getId(), null, "Inconnu", null, "Inconnu",
                null, f.getRating(), f.getComment(),
                f.getResponse(), f.getResponseDate(), f.getCreatedAt()
            );
        }
    }
}
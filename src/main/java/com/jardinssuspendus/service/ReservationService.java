package com.jardinssuspendus.service;

import com.jardinssuspendus.dto.request.ReservationRequest;
import com.jardinssuspendus.dto.response.ReservationResponse;
import com.jardinssuspendus.entity.Reservation;
import com.jardinssuspendus.entity.Room;
import com.jardinssuspendus.entity.User;
import com.jardinssuspendus.entity.enums.ReservationStatus;
import com.jardinssuspendus.exception.BadRequestException;
import com.jardinssuspendus.exception.ResourceNotFoundException;
import com.jardinssuspendus.repository.ReservationRepository;
import com.jardinssuspendus.repository.RoomRepository;
import com.jardinssuspendus.util.DateValidator;
import com.jardinssuspendus.util.PriceCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    @Autowired private ReservationRepository reservationRepository;
    @Autowired private RoomRepository        roomRepository;
    @Autowired private UserService           userService;
    @Autowired private EmailService          emailService;

    // ── Lecture ───────────────────────────────────────────────────────────────

    public List<ReservationResponse> getMyReservations() {
        User currentUser = userService.getCurrentUser();
        // JOIN FETCH garantit que user + room sont chargés
        return reservationRepository.findByUserId(currentUser.getId())
                .stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAllWithDetails()
                .stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ReservationResponse> getReservationsByStatus(ReservationStatus status) {
        return reservationRepository.findByStatus(status)
                .stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public ReservationResponse getReservationById(Long id) {
        Reservation reservation = findReservationById(id);
        validateAccess(reservation);
        return ReservationResponse.fromEntity(reservation);
    }

    // ── Création ──────────────────────────────────────────────────────────────

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request) {
        DateValidator.validateReservationDates(request.getStartDate(), request.getEndDate());

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Chambre", "id", request.getRoomId()));

        if (!room.getAvailable()) {
            throw new BadRequestException("Cette chambre n'est pas disponible à la réservation");
        }

        List<Reservation> conflicts = reservationRepository.findConflictingReservations(
                request.getRoomId(), request.getStartDate(), request.getEndDate());
        if (!conflicts.isEmpty()) {
            throw new BadRequestException("La chambre est déjà réservée pour ces dates");
        }

        int totalGuests = request.getAdults()
                + (request.getChildren6To12() != null ? request.getChildren6To12() : 0)
                + (request.getChildren0To5()  != null ? request.getChildren0To5()  : 0);
        if (totalGuests > room.getCapacity()) {
            throw new BadRequestException(
                "La chambre ne peut accueillir que " + room.getCapacity() + " personnes maximum");
        }

        BigDecimal totalPrice = PriceCalculator.calculateTotalPrice(
                room.getPrice(),
                request.getStartDate(),
                request.getEndDate(),
                request.getAdults(),
                request.getChildren6To12() != null ? request.getChildren6To12() : 0,
                request.getChildren0To5()  != null ? request.getChildren0To5()  : 0
        );

        User currentUser = userService.getCurrentUser();

        Reservation reservation = new Reservation();
        reservation.setUser(currentUser);
        reservation.setRoom(room);
        reservation.setStartDate(request.getStartDate());
        reservation.setEndDate(request.getEndDate());
        reservation.setAdults(request.getAdults());
        reservation.setChildren6To12(request.getChildren6To12() != null ? request.getChildren6To12() : 0);
        reservation.setChildren0To5(request.getChildren0To5()   != null ? request.getChildren0To5()  : 0);
        reservation.setTotalPrice(totalPrice);
        reservation.setSpecialRequests(request.getSpecialRequests());
        reservation.setStatus(ReservationStatus.EN_ATTENTE);

        Reservation saved = reservationRepository.save(reservation);

        // Recharger avec JOIN FETCH pour que fromEntity fonctionne
        Reservation reloaded = findReservationById(saved.getId());
        return ReservationResponse.fromEntity(reloaded);
    }

    // ── Modification ──────────────────────────────────────────────────────────

    @Transactional
    public ReservationResponse updateReservation(Long id, ReservationRequest request) {
        Reservation reservation = findReservationById(id);
        validateAccess(reservation);

        if (!reservation.canBeModified()) {
            throw new BadRequestException("Modification impossible : moins de 48h avant la date de départ");
        }
        if (reservation.getStatus() != ReservationStatus.EN_ATTENTE) {
            throw new BadRequestException("Seules les réservations EN_ATTENTE peuvent être modifiées");
        }

        DateValidator.validateReservationDates(request.getStartDate(), request.getEndDate());

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Chambre", "id", request.getRoomId()));

        List<Reservation> conflicts = reservationRepository.findConflictingReservations(
                request.getRoomId(), request.getStartDate(), request.getEndDate());
        conflicts.removeIf(c -> c.getId().equals(id));
        if (!conflicts.isEmpty()) {
            throw new BadRequestException("La chambre est déjà réservée pour ces dates");
        }

        BigDecimal totalPrice = PriceCalculator.calculateTotalPrice(
                room.getPrice(), request.getStartDate(), request.getEndDate(),
                request.getAdults(),
                request.getChildren6To12() != null ? request.getChildren6To12() : 0,
                request.getChildren0To5()  != null ? request.getChildren0To5()  : 0);

        reservation.setRoom(room);
        reservation.setStartDate(request.getStartDate());
        reservation.setEndDate(request.getEndDate());
        reservation.setAdults(request.getAdults());
        reservation.setChildren6To12(request.getChildren6To12() != null ? request.getChildren6To12() : 0);
        reservation.setChildren0To5(request.getChildren0To5()   != null ? request.getChildren0To5()  : 0);
        reservation.setTotalPrice(totalPrice);
        reservation.setSpecialRequests(request.getSpecialRequests());

        return ReservationResponse.fromEntity(reservationRepository.save(reservation));
    }

    // ── Annulation ────────────────────────────────────────────────────────────

    @Transactional
    public void cancelReservation(Long id) {
        Reservation reservation = findReservationById(id);
        validateAccess(reservation);

        if (!reservation.canBeCancelled()) {
            throw new BadRequestException(
                "Annulation impossible : moins de 48h avant le départ ou statut incorrect");
        }

        reservation.setStatus(ReservationStatus.ANNULEE);
        reservationRepository.save(reservation);

        try { emailService.sendCancellationEmail(reservation); }
        catch (Exception e) { /* Email non bloquant */ }
    }

    // ── Actions admin ─────────────────────────────────────────────────────────

    @Transactional
    public void validateReservation(Long id) {
        Reservation reservation = findReservationById(id);
        reservation.setStatus(ReservationStatus.VALIDEE);
        reservationRepository.save(reservation);
    }

    @Transactional
    public ReservationResponse reportReservation(Long id, ReservationRequest request) {
        Reservation reservation = findReservationById(id);
        reservation.setStartDate(request.getStartDate());
        reservation.setEndDate(request.getEndDate());
        return ReservationResponse.fromEntity(reservationRepository.save(reservation));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    Reservation findReservationById(Long id) {
        return reservationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation", "id", id));
    }

    private void validateAccess(Reservation reservation) {
        User currentUser = userService.getCurrentUser();
        if (!currentUser.isAdmin() && !reservation.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Vous n'avez pas accès à cette réservation");
        }
    }
}
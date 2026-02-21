package com.jardinssuspendus.repository;

import com.jardinssuspendus.entity.Reservation;
import com.jardinssuspendus.entity.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // ── JOIN FETCH pour charger user + room en même temps ─────────────────────

    @Query("SELECT r FROM Reservation r JOIN FETCH r.user JOIN FETCH r.room WHERE r.user.id = :userId ORDER BY r.createdAt DESC")
    List<Reservation> findByUserId(@Param("userId") Long userId);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.user JOIN FETCH r.room WHERE r.room.id = :roomId")
    List<Reservation> findByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.user JOIN FETCH r.room WHERE r.status = :status ORDER BY r.createdAt DESC")
    List<Reservation> findByStatus(@Param("status") ReservationStatus status);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.user JOIN FETCH r.room ORDER BY r.createdAt DESC")
    List<Reservation> findAllWithDetails();

    @Query("SELECT r FROM Reservation r JOIN FETCH r.user JOIN FETCH r.room WHERE r.id = :id")
    Optional<Reservation> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.user JOIN FETCH r.room " +
           "WHERE r.user.id = :userId AND r.status = :status")
    List<Reservation> findByUserIdAndStatus(
        @Param("userId") Long userId,
        @Param("status") ReservationStatus status
    );

    // ── Conflits de dates ──────────────────────────────────────────────────────
    @Query("SELECT r FROM Reservation r " +
           "WHERE r.room.id = :roomId " +
           "AND r.status IN ('EN_COURS', 'VALIDEE') " +
           "AND r.startDate <= :endDate " +
           "AND r.endDate >= :startDate")
    List<Reservation> findConflictingReservations(
        @Param("roomId") Long roomId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // ── Statistiques ───────────────────────────────────────────────────────────
    @Query("SELECT COUNT(r) FROM Reservation r " +
           "WHERE r.createdAt BETWEEN :startDate AND :endDate " +
           "AND r.status = 'VALIDEE'")
    Long countValidatedReservationsBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT r FROM Reservation r JOIN FETCH r.user JOIN FETCH r.room " +
           "WHERE r.startDate BETWEEN :startDate AND :endDate " +
           "AND r.status IN ('EN_COURS', 'VALIDEE') " +
           "ORDER BY r.startDate ASC")
    List<Reservation> findReservationsBetweenDates(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT SUM(r.adults) FROM Reservation r " +
           "WHERE r.createdAt BETWEEN :startDate AND :endDate " +
           "AND r.status = 'VALIDEE'")
    Long countAdultsBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT SUM(r.children6To12 + r.children0To5) FROM Reservation r " +
           "WHERE r.createdAt BETWEEN :startDate AND :endDate " +
           "AND r.status = 'VALIDEE'")
    Long countChildrenBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
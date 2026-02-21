package com.jardinssuspendus.repository;

import com.jardinssuspendus.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // ── JOIN FETCH sur toutes les requêtes pour éviter LazyInitializationException ──

    @Query("SELECT f FROM Feedback f JOIN FETCH f.user JOIN FETCH f.room WHERE f.room.id = :roomId ORDER BY f.createdAt DESC")
    List<Feedback> findByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT f FROM Feedback f JOIN FETCH f.user JOIN FETCH f.room WHERE f.user.id = :userId ORDER BY f.createdAt DESC")
    List<Feedback> findByUserId(@Param("userId") Long userId);

    @Query("SELECT f FROM Feedback f JOIN FETCH f.user JOIN FETCH f.room LEFT JOIN FETCH f.reservation WHERE f.reservation.id = :reservationId")
    Optional<Feedback> findByReservationId(@Param("reservationId") Long reservationId);

    @Query("SELECT f FROM Feedback f JOIN FETCH f.user JOIN FETCH f.room WHERE f.room.id = :roomId ORDER BY f.createdAt DESC")
    List<Feedback> findByRoomIdOrderByCreatedAtDesc(@Param("roomId") Long roomId);

    @Query("SELECT f FROM Feedback f JOIN FETCH f.user JOIN FETCH f.room ORDER BY f.createdAt DESC")
    List<Feedback> findAllWithDetails();

    @Query("SELECT f FROM Feedback f JOIN FETCH f.user JOIN FETCH f.room WHERE f.id = :id")
    Optional<Feedback> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT f FROM Feedback f JOIN FETCH f.user JOIN FETCH f.room WHERE f.response IS NULL ORDER BY f.createdAt DESC")
    List<Feedback> findUnansweredFeedbacks();

    @Query("SELECT f FROM Feedback f JOIN FETCH f.user JOIN FETCH f.room WHERE f.response IS NOT NULL ORDER BY f.responseDate DESC")
    List<Feedback> findAnsweredFeedbacks();

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.room.id = :roomId")
    Double getAverageRatingByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.room.id = :roomId")
    Long countByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT f FROM Feedback f JOIN FETCH f.user JOIN FETCH f.room WHERE f.rating >= :minRating ORDER BY f.createdAt DESC")
    List<Feedback> findByMinimumRating(@Param("minRating") Integer minRating);
}
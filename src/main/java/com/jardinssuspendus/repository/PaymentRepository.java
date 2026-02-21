package com.jardinssuspendus.repository;

import com.jardinssuspendus.entity.Payment;
import com.jardinssuspendus.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByReservationId(Long reservationId);

    // JOIN FETCH pour charger la réservation en même temps
    @Query("SELECT p FROM Payment p JOIN FETCH p.reservation WHERE p.paypalOrderId = :orderId")
    Optional<Payment> findByPaypalOrderIdWithReservation(@Param("orderId") String orderId);

    Optional<Payment> findByPaypalOrderId(String paypalOrderId);

    List<Payment> findByStatus(PaymentStatus status);

    // Calcul CA — utilise PaymentStatus enum (pas une string littérale)
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.status = com.jardinssuspendus.entity.enums.PaymentStatus.PAYE " +
           "AND p.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // Comptage paiements réussis
    @Query("SELECT COUNT(p) FROM Payment p " +
           "WHERE p.status = com.jardinssuspendus.entity.enums.PaymentStatus.PAYE " +
           "AND p.paymentDate BETWEEN :startDate AND :endDate")
    Long countSuccessfulPaymentsBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT p FROM Payment p WHERE p.status = :status ORDER BY p.paymentDate DESC")
    List<Payment> findByStatusOrderByPaymentDateDesc(@Param("status") PaymentStatus status);

    // Diagnostic : tous les paiements avec leurs infos
    @Query("SELECT p FROM Payment p JOIN FETCH p.reservation ORDER BY p.createdAt DESC")
    List<Payment> findAllWithReservation();
}
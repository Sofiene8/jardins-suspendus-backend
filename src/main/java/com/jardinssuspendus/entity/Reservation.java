package com.jardinssuspendus.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jardinssuspendus.entity.enums.ReservationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "room", "payment", "feedback"})
@EqualsAndHashCode(exclude = {"user", "room", "payment", "feedback"})
@EntityListeners(AuditingEntityListener.class)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @NotNull(message = "La date d'arrivée est obligatoire")
    @Column(nullable = false)
    private LocalDate startDate;

    @NotNull(message = "La date de départ est obligatoire")
    @Column(nullable = false)
    private LocalDate endDate;

    @Min(value = 1, message = "Au moins 1 adulte requis")
    @Column(nullable = false)
    private Integer adults = 1;

    @Min(value = 0)
    @Column(nullable = false)
    private Integer children6To12 = 0;

    @Min(value = 0)
    @Column(nullable = false)
    private Integer children0To5 = 0;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.EN_ATTENTE;

    @Column(length = 500)
    private String specialRequests;

    @JsonIgnore
    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment;

    @JsonIgnore
    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Feedback feedback;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public int getTotalGuests() {
        return adults + children6To12 + children0To5;
    }

    public long getNights() {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
    }

    public boolean canBeModified() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime departureDateTime = endDate.atStartOfDay();
        return now.plusHours(48).isBefore(departureDateTime);
    }

    public boolean canBeCancelled() {
        return canBeModified() &&
               (status == ReservationStatus.EN_ATTENTE || status == ReservationStatus.VALIDEE);
    }
}
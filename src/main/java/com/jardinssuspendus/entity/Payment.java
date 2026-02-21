package com.jardinssuspendus.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jardinssuspendus.entity.enums.PaymentStatus;
import jakarta.persistence.*;
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
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"reservation"})
@EqualsAndHashCode(exclude = {"reservation"})
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;

    @Column(unique = true)
    private String paypalOrderId;

    private String paypalPayerId;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.EN_ATTENTE;

    @Column(length = 10)
    private String currency = "USD";

    @Column(length = 500)
    private String paymentDetails;

    private LocalDateTime paymentDate;

    @Column(length = 1000)
    private String errorMessage;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public boolean isSuccessful() { return this.status == PaymentStatus.PAYE; }
    public boolean isFailed()     { return this.status == PaymentStatus.ECHOUE; }
    public boolean isPending()    { return this.status == PaymentStatus.EN_ATTENTE; }
}
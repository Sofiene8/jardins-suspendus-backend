package com.jardinssuspendus.dto.response;

import com.jardinssuspendus.entity.Payment;
import com.jardinssuspendus.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long reservationId;
    private String paypalOrderId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private LocalDateTime paymentDate;
    private String errorMessage;
    private LocalDateTime createdAt;

    public static PaymentResponse fromEntity(Payment payment) {
        return new PaymentResponse(
            payment.getId(),
            payment.getReservation().getId(),
            payment.getPaypalOrderId(),
            payment.getAmount(),
            payment.getCurrency(),
            payment.getStatus(),
            payment.getPaymentDate(),
            payment.getErrorMessage(),
            payment.getCreatedAt()
        );
    }
}
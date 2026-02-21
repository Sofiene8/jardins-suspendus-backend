package com.jardinssuspendus.dto.response;

import com.jardinssuspendus.entity.Reservation;
import com.jardinssuspendus.entity.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long roomId;
    private String roomTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer adults;
    private Integer children6To12;
    private Integer children0To5;
    private Integer totalGuests;
    private Long nights;
    private BigDecimal totalPrice;
    private ReservationStatus status;
    private String specialRequests;
    private Boolean canBeModified;
    private Boolean canBeCancelled;
    private LocalDateTime createdAt;

    public static ReservationResponse fromEntity(Reservation reservation) {
        return new ReservationResponse(
            reservation.getId(),
            reservation.getUser().getId(),
            reservation.getUser().getName(),
            reservation.getRoom().getId(),
            reservation.getRoom().getTitle(),
            reservation.getStartDate(),
            reservation.getEndDate(),
            reservation.getAdults(),
            reservation.getChildren6To12(),
            reservation.getChildren0To5(),
            reservation.getTotalGuests(),
            reservation.getNights(),
            reservation.getTotalPrice(),
            reservation.getStatus(),
            reservation.getSpecialRequests(),
            reservation.canBeModified(),
            reservation.canBeCancelled(),
            reservation.getCreatedAt()
        );
    }
}
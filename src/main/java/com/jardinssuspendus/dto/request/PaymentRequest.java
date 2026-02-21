package com.jardinssuspendus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "L'ID de la réservation est obligatoire")
    private Long reservationId;

    @NotBlank(message = "L'ID de commande PayPal est obligatoire")
    private String paypalOrderId;

    private String paypalPayerId;

    private String currency = "DT";
}
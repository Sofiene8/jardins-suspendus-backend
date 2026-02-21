package com.jardinssuspendus.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequest {

    @NotNull(message = "L'ID de la chambre est obligatoire")
    private Long roomId;

    @NotNull(message = "La date d'arrivée est obligatoire")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "La date de départ est obligatoire")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @NotNull(message = "Le nombre d'adultes est obligatoire")
    @Min(value = 1, message = "Au moins 1 adulte est requis")
    @Max(value = 10, message = "Maximum 10 adultes")
    private Integer adults;

    @Min(value = 0, message = "Le nombre d'enfants ne peut pas être négatif")
    @Max(value = 10, message = "Maximum 10 enfants (6-12 ans)")
    private Integer children6To12 = 0;

    @Min(value = 0, message = "Le nombre d'enfants ne peut pas être négatif")
    @Max(value = 10, message = "Maximum 10 enfants (0-5 ans)")
    private Integer children0To5 = 0;

    @Size(max = 500, message = "Les demandes spéciales ne peuvent pas dépasser 500 caractères")
    private String specialRequests;
}
package com.jardinssuspendus.dto.request;

import jakarta.validation.constraints.*;
import lombok.*; // Importe tout lombok d'un coup (Getter, Setter, etc.)
import java.math.BigDecimal;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor
@Builder
public class RoomRequest {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 3, max = 200, message = "Le titre doit contenir entre 3 et 200 caractères")
    private String title;

    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
    private String description;

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
    private BigDecimal price;

    @NotNull(message = "La capacité est obligatoire")
    @Min(value = 1, message = "La capacité minimale est 1 personne")
    @Max(value = 10, message = "La capacité maximale est 10 personnes")
    private Integer capacity;

    @Builder.Default // Indispensable pour que le "true" soit conservé lors de l'utilisation du Builder
    private Boolean available = true;
}
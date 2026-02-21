package com.jardinssuspendus.dto.response;

import com.jardinssuspendus.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private List<String> images;
    private Boolean available;
    private Integer capacity;
    private Double averageRating;
    private Long totalReviews;
    private LocalDateTime createdAt;

    public static RoomResponse fromEntity(Room room) {
        return new RoomResponse(
            room.getId(),
            room.getTitle(),
            room.getDescription(),
            room.getPrice(),
            room.getImages(),
            room.getAvailable(),
            room.getCapacity(),
            null, // averageRating sera calculé par le service
            null, // totalReviews sera calculé par le service
            room.getCreatedAt()
        );
    }

    public static RoomResponse fromEntity(Room room, Double averageRating, Long totalReviews) {
        return new RoomResponse(
            room.getId(),
            room.getTitle(),
            room.getDescription(),
            room.getPrice(),
            room.getImages(),
            room.getAvailable(),
            room.getCapacity(),
            averageRating,
            totalReviews,
            room.getCreatedAt()
        );
    }
}
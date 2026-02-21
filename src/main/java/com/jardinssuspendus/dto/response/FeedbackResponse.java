package com.jardinssuspendus.dto.response;

import com.jardinssuspendus.entity.Feedback;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long roomId;
    private String roomTitle;
    private Long reservationId;
    private Integer rating;
    private String comment;
    private String response;
    private LocalDateTime responseDate;
    private LocalDateTime createdAt;

    public static FeedbackResponse fromEntity(Feedback feedback) {
        return new FeedbackResponse(
            feedback.getId(),
            feedback.getUser().getId(),
            feedback.getUser().getName(),
            feedback.getRoom().getId(),
            feedback.getRoom().getTitle(),
            feedback.getReservation() != null ? feedback.getReservation().getId() : null,
            feedback.getRating(),
            feedback.getComment(),
            feedback.getResponse(),
            feedback.getResponseDate(),
            feedback.getCreatedAt()
        );
    }
}
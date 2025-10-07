package com.reviewmoderaion.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponseDto(
    UUID reviewId,
    String originalText,
    Recommendation recommendation,
    double confidenceScore,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    LocalDateTime processedAt
) {

    public enum Recommendation {
        APPROVE, REJECT, MANUAL_REVIEW
    }
}

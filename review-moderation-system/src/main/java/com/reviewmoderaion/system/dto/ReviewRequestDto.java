package com.reviewmoderaion.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReviewRequestDto(
    @NotBlank(message = "Review is required")
    @Size(max = 5000, message = "Review must be less than 5000 characters")
    String review,

    // optional
    String userId,

    // optional
    String productId
) {
}

package com.reviewmoderaion.system.controller.v1;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.reviewmoderaion.system.dto.ReviewRequestDto;
import com.reviewmoderaion.system.dto.ReviewResponseDto;
import com.reviewmoderaion.system.service.ReviewModerationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewsController {

    private final ReviewModerationService reviewModerationService;

    @PostMapping("/moderate")
    public ResponseEntity<ReviewResponseDto> moderateReview(@Valid @RequestBody ReviewRequestDto review) {
        ReviewResponseDto response = reviewModerationService.moderateReview(review);
        return ResponseEntity.ok(response);
    }
}

package com.reviewmoderaion.system.controller.v1;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.reviewmoderaion.system.dto.ReviewRequestDto;
import com.reviewmoderaion.system.dto.ReviewResponseDto;
import com.reviewmoderaion.system.service.ReviewClassificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewClassificationController {

    private final ReviewClassificationService reviewClassificationService;

    @PostMapping("/classify")
    public ResponseEntity<ReviewResponseDto> classifyReview(@Valid @RequestBody ReviewRequestDto review) {
        ReviewResponseDto response = reviewClassificationService.classifyReview(review);
        return ResponseEntity.ok(response);
    }
}

package com.reviewmoderaion.system.service;

import org.springframework.stereotype.Service;

import com.reviewmoderaion.system.dto.ReviewRequestDto;
import com.reviewmoderaion.system.dto.ReviewResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewModerationService {

    private final PythonModelService pythonModelService;

    public ReviewResponseDto moderateReview(ReviewRequestDto review) {
        String text = review.review();

        try {
            log.debug("Starting review moderation for text length: {} characters", text.length());

            Map<String, Object> modelResponse = pythonModelService.executePythonModelScript(review);

            Integer label = (Integer) modelResponse.get("label");
            Double probability = (Double) modelResponse.get("probability");

            log.debug("Model result - label: {}, probability: {}", label, probability);

            ReviewResponseDto.Recommendation recommendation = determineRecommendation(label);

            return new ReviewResponseDto(
                UUID.randomUUID(),
                text,
                recommendation,
                probability,
                LocalDateTime.now()
            );

        } catch (Exception e) {
            log.error("Error during review moderation: {}", e.getMessage(), e);

            return new ReviewResponseDto(
                UUID.randomUUID(),
                text,
                ReviewResponseDto.Recommendation.MANUAL_REVIEW,
                0.0,
                LocalDateTime.now()
            );
        }
    }

    private ReviewResponseDto.Recommendation determineRecommendation(Integer label) {
        switch (label) {
            case 1:
                return ReviewResponseDto.Recommendation.APPROVE;
            case 0:
                return ReviewResponseDto.Recommendation.MANUAL_REVIEW;
            case 2:
                return ReviewResponseDto.Recommendation.REJECT;
            default:
                log.warn("Unknown model class: {}", label);
                return ReviewResponseDto.Recommendation.MANUAL_REVIEW;
        }
    }
}

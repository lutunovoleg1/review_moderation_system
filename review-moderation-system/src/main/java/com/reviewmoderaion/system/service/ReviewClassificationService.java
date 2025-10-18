package com.reviewmoderaion.system.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
public class ReviewClassificationService {
    
    private final RestTemplate restTemplate;

    @Value("${prediction.pipeline.url}")
    private String predictionPipelineUrl;

    public ReviewResponseDto classifyReview(ReviewRequestDto review) {
        String text = review.review();

        try {
            log.debug("Starting review classification for text length: {} characters", text.length());

            Map<String, String> payload = Map.of("text", text);
            log.error("PAYLOAD: {}", payload);

            try {
                ResponseEntity<Map> response = restTemplate.postForEntity(
                    predictionPipelineUrl, 
                    payload,
                    Map.class
                );

                Map<String, Object> result = response.getBody();
                
                if (result == null) {
                    return new ReviewResponseDto(
                        UUID.randomUUID(),
                        text,
                        ReviewResponseDto.Recommendation.MANUAL_REVIEW,
                        0.0,
                        LocalDateTime.now()
                    );
                }

                Integer label = result.containsKey("label") ? 
                    ((Number) result.get("label")).intValue() : null;
                Double probability = result.containsKey("probability") ? 
                    ((Number) result.get("probability")).doubleValue() : null;

                log.error("EXTRACTED LABEL: {}", label);
                log.error("EXTRACTED PROBABILITY: {}", probability);

                ReviewResponseDto.Recommendation recommendation = label != null ? 
                    determineRecommendation(label) : 
                    ReviewResponseDto.Recommendation.MANUAL_REVIEW;

                return new ReviewResponseDto(
                    UUID.randomUUID(),
                    text,
                    recommendation,
                    probability != null ? probability : 0.0,
                    LocalDateTime.now()
                );

            } catch (Exception e) {
                log.error("DETAILED RESTTEMPLATE ERROR: {}", e.getMessage(), e);
                throw e;
            }

        } catch (Exception e) {
            log.error("ERROR DURING REVIEW MODERATION: {}", e.getMessage(), e);

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

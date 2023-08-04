package dev.sodev.domain.review.dto;

import dev.sodev.domain.review.Review;
import lombok.Builder;

@Builder
public record ReviewDto(
        Long reviewId,
        String peerReview
) {

    public static ReviewDto of(Review review) {
        return ReviewDto.builder()
                .reviewId(review.getReviewId())
                .peerReview(review.getPeerReview())
                .build();
    }
}

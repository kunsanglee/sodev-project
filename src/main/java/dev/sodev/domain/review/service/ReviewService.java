package dev.sodev.domain.review.service;

import dev.sodev.domain.review.dto.ReviewDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ReviewService {

    Slice<ReviewDto> getReviews(Long memberId, Pageable pageable);
}

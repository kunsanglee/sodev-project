package dev.sodev.domain.review.repository.query;

import dev.sodev.domain.review.dto.ReviewDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ReviewCustomRepository {

    Slice<ReviewDto> getMemberReviews(Long memberId, Pageable pageable);
}

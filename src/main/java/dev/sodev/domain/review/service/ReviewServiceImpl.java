package dev.sodev.domain.review.service;

import dev.sodev.domain.review.dto.ReviewDto;
import dev.sodev.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    @Override
    public Slice<ReviewDto> getReviews(Long memberId, Pageable pageable) {
        return reviewRepository.getMemberReviews(memberId, pageable);
    }
}

package dev.sodev.domain.review.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.sodev.domain.review.QReview;
import dev.sodev.domain.review.dto.ReviewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;

import static dev.sodev.domain.review.QReview.*;

@Repository
@RequiredArgsConstructor
public class ReviewCustomRepositoryImpl implements ReviewCustomRepository {

    private final JPAQueryFactory queryFactory;

    public Slice<ReviewDto> getMemberReviews(Long memberId, Pageable pageable) {
        List<ReviewDto> result = queryFactory.select(Projections.constructor(ReviewDto.class, review.reviewId, review.peerReview))
                .from(review)
                .where(review.member.id.eq(memberId))
                .orderBy(review.reviewId.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = false;
        if (result.size() == pageable.getPageSize() + 1) {
            hasNext = true;
            result.remove(result.size() - 1);
        }

        return new SliceImpl<>(result, pageable, hasNext);
    }
}

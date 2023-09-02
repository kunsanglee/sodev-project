package dev.sodev.domain.review.repository;

import dev.sodev.domain.review.Review;
import dev.sodev.domain.review.repository.query.ReviewCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewCustomRepository {

}

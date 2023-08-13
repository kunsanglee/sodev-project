package dev.sodev.domain.review;

import dev.sodev.domain.BaseEntity;
import dev.sodev.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "review", indexes = {
        @Index(name = "idx_review_member", columnList = "member_id")
})
@Entity
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String peerReview;

    public static Review of(Member member, String peerReview) {
        return new Review(null, member, peerReview);
    }
}

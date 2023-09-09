package dev.sodev.domain.follow;

import dev.sodev.domain.BaseEntity;
import dev.sodev.domain.member.Member;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "follow", indexes = {
        @Index(name = "idx_follow_from_member", columnList = "from_member"),
        @Index(name = "idx_follow_to_member", columnList = "to_member")
})
@Entity
public class Follow extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "follow_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_member")
    private Member fromMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_member")
    private Member toMember;

    public static Follow follow(Member fromMember, Member toMember) {
        Follow follow = Follow.builder()
                .fromMember(fromMember)
                .toMember(toMember)
                .build();
        fromMember.getFollowing().add(follow);
        toMember.getFollowers().add(follow);
        return follow;
    }

    public static Follow unfollow(Member fromMember, Member toMember) {
        Follow follow = fromMember.getFollowing().stream()
                .filter(f -> f.getToMember().equals(toMember))
                .findAny()
                .orElseThrow(() -> new SodevApplicationException(ErrorCode.FOLLOW_NOT_FOUND));
        fromMember.getFollowing().remove(follow);
        toMember.getFollowers().remove(follow);
        return follow;
    }
}

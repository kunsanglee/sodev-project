package dev.sodev.domain.follow;

import dev.sodev.domain.BaseEntity;
import dev.sodev.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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

    public void follow(Member member) {
        this.toMember = member;
        member.getFollowers().add(this);
    }

    public void unfollow(Member toMember) {
        this.toMember = toMember;
        toMember.getFollowers().remove(this);
    }
}

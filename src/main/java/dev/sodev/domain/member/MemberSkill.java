package dev.sodev.domain.member;

import dev.sodev.domain.skill.Skill;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "member_skill", indexes = {
        @Index(name = "idx_member_skill_member", columnList = "member_id"),
        @Index(name = "idx_member_skill_skill", columnList = "skill_id")
})
@Entity
public class MemberSkill {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_skill_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id")
    private Skill skill;


}

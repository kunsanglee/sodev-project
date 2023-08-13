package dev.sodev.domain.likes;

import dev.sodev.domain.BaseEntity;
import dev.sodev.domain.project.Project;
import dev.sodev.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "likes", indexes = {
        @Index(name = "idx_likes_member", columnList = "member_id"),
        @Index(name = "idx_likes_project", columnList = "project_id"),
        @Index(name = "idx_likes_member_project", columnList = "member_id, project_id")
})
@Entity
public class Likes extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    public static Likes of(Member member, Project project) {
        return new Likes(null, member, project);
    }
}

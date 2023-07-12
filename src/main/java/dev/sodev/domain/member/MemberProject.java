package dev.sodev.domain.member;


import dev.sodev.domain.BaseEntity;
import dev.sodev.domain.project.Project;
import dev.sodev.domain.enums.ProjectState;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MemberProject extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_project_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Enumerated(EnumType.STRING)
    private ProjectState state;

    public static MemberProject of(Member member, Project project)      {
        return new MemberProject(null, member,project,ProjectState.RECRUIT);
    }
}

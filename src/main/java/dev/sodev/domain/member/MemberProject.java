package dev.sodev.domain.member;


import dev.sodev.domain.BaseEntity;
import dev.sodev.domain.enums.ProjectRole;
import dev.sodev.domain.project.Project;
import dev.sodev.domain.enums.ProjectState;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
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

//    @Enumerated(EnumType.STRING)
//    private ProjectState state;

    @Enumerated(EnumType.STRING)
    private ProjectRole role;

    public static MemberProject of(Member member, Project project, ProjectRole role) {
        return new MemberProject(null, member, project, role);
    }

    public void addProjectAndMember(Member member, Project project) { // 회원의 프로젝트 참여 리스트, 프로젝트의 참여 회원 추가
//        this.member = member;
//        this.project = project;
        member.getMemberProject().add(this);
        project.getMembers().add(this);
    }

    public void addProjectAndApplicant(Member member, Project project) { // 회원의 프로젝트 지원 리스트, 프로젝트의 지원자 리스트에 추
//        this.member = member;
//        this.project = project;
        member.getApplies().add(this);
        project.getApplicants().add(this);
    }
}

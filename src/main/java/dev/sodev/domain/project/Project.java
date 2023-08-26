package dev.sodev.domain.project;

import dev.sodev.domain.BaseEntity;
import dev.sodev.domain.comment.Comment;
import dev.sodev.domain.enums.ProjectRole;
import dev.sodev.domain.enums.ProjectState;
import dev.sodev.domain.likes.Likes;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.member.MemberProject;
import dev.sodev.domain.member.dto.MemberProjectDto;
import dev.sodev.domain.project.dto.requset.ProjectInfoRequest;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Project extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long id;
    private String title;
    private String content;
    private Integer be;
    private Integer fe;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private ProjectState state = ProjectState.RECRUIT;

    private String registeredBy;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime recruitDate;

    @Builder.Default
    @OneToMany(mappedBy = "project")
    private List<MemberProject> members = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "project")
    private List<MemberProject> applicants = new ArrayList<>(); // 지원자 리스트

    @Builder.Default
    @OneToMany(mappedBy = "project")
    private List<ProjectSkill> skills = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "project")
    private List<Likes> likes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "project")
    private List<Comment> comments = new ArrayList<>();


    public void startProject() {
        if (!this.state.equals(ProjectState.RECRUIT)) {
            throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "프로젝트 모집 단계에서만 시작할 수 있습니다.");
        }
        this.state = ProjectState.PROGRESS;
    }

    public void completeProject() {
        if (!this.state.equals(ProjectState.PROGRESS)) {
            throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "프로젝트 진행 단계에서만 완료할 수 있습니다.");
        }
        this.state = ProjectState.COMPLETE;
    }

    // 지원자의 memberProject 반환.
    public MemberProject getApplicantMemberProject(Member applicant) {
        return this.getApplicants().stream()
                .filter(mp -> mp.getMember().equals(applicant))
                .findFirst()
                .orElseThrow(() -> new SodevApplicationException(ErrorCode.BAD_REQUEST));
    }

    // Member 의 MemberProject 존재하는지 확인.
    public MemberProject getMemberProject(Member member) {
        return this.members.stream()
                .filter(mp -> mp.getMember().equals(member))
                .findAny()
                .orElseThrow(() -> new SodevApplicationException(ErrorCode.BAD_REQUEST));
    }

    // 해당 프로젝트의 직무 자리가 남아있는지 확인.
    public void isJoinable(MemberProjectDto memberProjectDto) {
        long size = this.getMembers().stream()
                .filter(m -> m.getProjectRole().getRoleType().equals(memberProjectDto.role().getRoleType()))
                .count();

        if (memberProjectDto.role().getRoleType().equals(ProjectRole.RoleType.BE)) { // 백앤드인 경우
            if (this.getBe() - size <= 0) {
                throw new SodevApplicationException(ErrorCode.RECRUITMENT_EXCEED);
            }
        } else { // 프론트앤드인 경우
            if (this.getFe() - size <= 0) {
                throw new SodevApplicationException(ErrorCode.RECRUITMENT_EXCEED);
            }
        }
    }

    // 동료평가시 평가하려는 회원의 프로젝트가 완료된 상태인지, 같은 프로젝트를 진행했는지 확인.
    public void isEvaluationAvailable(Project writerProject) {
        if (!this.getState().equals(ProjectState.COMPLETE) ||
                !this.getId().equals(writerProject.getId())) {
            throw new SodevApplicationException(ErrorCode.BAD_REQUEST);
        }
    }

    public List<Member> alarmsToMember() {
        return this.getMembers().stream()
                .filter(mp -> !mp.getProjectRole().getRole().equals(ProjectRole.Role.APPLICANT))
                .map(MemberProject::getMember)
                .distinct()
                .toList();
    }

    public List<Member> alarmsToLikes() {
        return this.getLikes().stream().map(Likes::getMember).toList();
    }

    public void update(ProjectInfoRequest request) {
        updateTitle(request.title());
        updateContent(request.content());
        updateBe(request.be());
        updateFe(request.fe());
        updateStartDate(request.startDate());
        updateEndDate(request.endDate());
        updateRecruitDate(request.recruitDate());
    }

    private void updateTitle(String title) {
        if (title != null) {
            this.title = title;
        }
    }

    private void updateContent(String content) {
        if (content != null) {
            this.content = content;
        }
    }

    private void updateBe(Integer be) {
        if (be != null) {
            this.be = be;
        }
    }

    private void updateFe(Integer fe) {
        if (fe != null) {
            this.fe = fe;
        }
    }

    private void updateStartDate(LocalDateTime startDate) {
        if (startDate != null) {
            this.startDate = startDate;
        }
    }

    private void updateEndDate(LocalDateTime endDate) {
        if (endDate != null) {
            this.endDate = endDate;
        }
    }

    private void updateRecruitDate(LocalDateTime recruitDate) {
        if (recruitDate != null) {
            this.recruitDate = recruitDate;
        }
    }
}

package dev.sodev.domain.project;

import dev.sodev.domain.BaseEntity;
import dev.sodev.domain.comment.Comment;
import dev.sodev.domain.enums.ProjectState;
import dev.sodev.domain.likes.Likes;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.member.MemberProject;
import dev.sodev.domain.project.dto.requset.ProjectInfoRequest;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.web.bind.MethodArgumentNotValidException;

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
    @OneToMany(mappedBy = "project", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<MemberProject> members = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "project", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<MemberProject> applicants = new ArrayList<>(); // 지원자 리스트

    @Builder.Default
    @OneToMany(mappedBy = "project", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ProjectSkill> skills = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "project", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Likes> likes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "project", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();



    public void update(ProjectInfoRequest request) {
        this.title = request.title();
        this.content = request.content();
        this.be = request.be();
        this.fe = request.fe();
        this.startDate = request.start_date();
        this.endDate = request.end_date();
        this.recruitDate = request.recruit_date();
    }

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

}

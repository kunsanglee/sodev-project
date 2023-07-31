package dev.sodev.domain.project.dto;

import dev.sodev.domain.comment.dto.CommentDto;
import dev.sodev.domain.enums.ProjectState;
import dev.sodev.domain.likes.dto.LikesMemberDto;
import dev.sodev.domain.member.dto.MemberProjectDto;
import dev.sodev.domain.project.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDto {
    private Long id;
    private Integer be;
    private Integer fe;
    private ProjectState state;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime recruitDate;
    private String title;
    private String content;

    private List<SkillDto> skills = new ArrayList<>();
    private List<MemberProjectDto> members = new ArrayList<>();
    private List<MemberProjectDto> applicants = new ArrayList<>();
    private List<CommentDto> comments = new ArrayList<>();
    private List<LikesMemberDto> likes = new ArrayList<>();

    private String registeredBy;
    private String createdBy;
    private LocalDateTime createdAt;
    private String modifiedBy;
    private LocalDateTime modifiedAt;

    public static ProjectDto of(Project project) {
        return ProjectDto.builder()
                .id(project.getId())
                .be(project.getBe())
                .fe(project.getFe())
                .state(project.getState())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .recruitDate(project.getRecruitDate())
                .title(project.getTitle())
                .content(project.getContent())
                .build();
    }

    public void addLikes(List<LikesMemberDto> likes) {
        this.likes = likes;
    }

    public void addComments(List<CommentDto> comments) {
        this.comments = comments;
    }

    public void addMemberProjects(List<MemberProjectDto> memberProject) {
        this.members = memberProject;
    }

    public void addApplicants(List<MemberProjectDto> memberProject) {
        this.applicants = memberProject;
    }

}

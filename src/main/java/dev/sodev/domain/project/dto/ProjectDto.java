package dev.sodev.domain.project.dto;

import dev.sodev.domain.comment.dto.CommentDto;
import dev.sodev.domain.project.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDto {
    private Long projectId;
    private Integer be;
    private Integer fe;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime recruitDate;
    private String title;
    private String content;
    private List<SkillDto> skills;
    private List<CommentDto> comments;
    private Long likes;
    private String registeredBy;
    private String createdBy;
    private LocalDateTime createdAt;
    private String modifiedBy;
    private LocalDateTime modifiedAt;

    public static ProjectDto fromEntity(Project project) {
        return ProjectDto.builder()
                .projectId(project.getId())
                .be(project.getBe())
                .fe(project.getFe())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .recruitDate(project.getRecruitDate())
                .title(project.getTitle())
                .content(project.getContent())
                .build();
    }
}

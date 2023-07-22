package dev.sodev.domain.project.dto;

import dev.sodev.domain.comment.dto.CommentDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
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
    private String createdBy;
    private LocalDateTime createdAt;
    private String modifiedBy;
    private LocalDateTime modifiedAt;

}

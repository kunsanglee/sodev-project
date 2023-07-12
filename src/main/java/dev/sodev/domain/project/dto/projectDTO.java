package dev.sodev.domain.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class projectDTO {
        private Integer be;
    private Integer fe;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime recruitDate;
    private String title;
    private String content;
    private List<skillDTO> skills;
    private String createdBy;
    private LocalDateTime createdAt;
    private String modifiedBy;
    private LocalDateTime modifiedAt;

}

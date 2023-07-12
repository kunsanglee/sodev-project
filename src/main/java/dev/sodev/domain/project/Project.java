package dev.sodev.domain.project;

import dev.sodev.domain.BaseEntity;
import dev.sodev.domain.enums.ProjectState;
import dev.sodev.domain.project.dto.requset.ProjectInfoRequest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
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

    @Enumerated(EnumType.STRING)
    private ProjectState state;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime recruitDate;

    public static Project of(ProjectInfoRequest request){
        return Project.builder()
                .fe(request.fe())
                .be(request.be())
                .title(request.title())
                .content(request.content())
                .state(ProjectState.RECRUIT)
                .recruitDate(request.recruit_date())
                .startDate(request.start_date())
                .endDate(request.end_date())
                .build();
    }
}

package dev.sodev.domain.project;

import dev.sodev.domain.BaseEntity;
import dev.sodev.domain.comment.Comment;
import dev.sodev.domain.enums.ProjectState;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.project.dto.requset.ProjectInfoRequest;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.List;

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

    private String registeredBy;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime recruitDate;


    @OneToMany(mappedBy = "project")
    private List<Comment> comments;


    public static Project of(ProjectInfoRequest request, Member member){
        return Project.builder()
                .fe(request.fe())
                .be(request.be())
                .title(request.title())
                .content(request.content())
                .state(ProjectState.RECRUIT)
                .registeredBy(member.getNickName())
                .recruitDate(request.recruit_date())
                .startDate(request.start_date())
                .endDate(request.end_date())
                .build();
    }

    public void changeTitle(String newTitle) {
        nullAndEmptyCheck(newTitle);
        this.title = newTitle;
    }
    public void changeContent(String newContent) {
        nullAndEmptyCheck(newContent);
        this.content= newContent;
    }
//    public void changeFe(String newContent) {
//        nullAndEmptyCheck(newContent);
//        this.content= newContent;
//    }
//    public void changeBe(String newContent) {
//        nullAndEmptyCheck(newContent);
//        this.content= newContent;
//    }
//    public void changeRecruitDate(String newContent) {
//        nullAndEmptyCheck(newContent);
//        this.content= newContent;
//    }
//    public void changeStartDate(String newContent) {
//        nullAndEmptyCheck(newContent);
//        this.content= newContent;
//    }
//    public void changeEndDate(String newContent) {
//        nullAndEmptyCheck(newContent);
//        this.content= newContent;
//    }
//    private void nullAndEmptyCheck(String someString) {
//        if(someString.isBlank()) {
//            throw new IllegalArgumentException("공백은 입력 불가합니다.");
//        }
//    }

    private void nullAndEmptyCheck(String someString) {
        if(someString.isBlank()) {
            throw new IllegalArgumentException("공백은 입력 불가합니다.");
        }
    }
}

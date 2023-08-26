package dev.sodev.domain.project.dto.requset;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.sodev.domain.enums.ProjectState;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.project.Project;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Schema(description = "Project Feed Request")
public record ProjectInfoRequest(

        @Schema(description = "백엔드 인원수", example = "3")
        Integer be,

        @Schema(description = "프론트엔드 인원수", example = "2")
        Integer fe,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "프로젝트 시작 일시", example = "2023-08-30 09:00:00", type = "string")
        LocalDateTime startDate,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "프로젝트 종료 일시", example = "2023-09-30 18:00:00", type = "string")
        LocalDateTime endDate,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "프로젝트 모집 일시", example = "2023-08-25 00:00:00", type = "string")
        LocalDateTime recruitDate,

        @Schema(description = "프로젝트 제목", example = "간단한 SNS 플랫폼 프로젝트 진행하려고 합니다~")
        String title,

        @Schema(description = "프로젝트 내용", example = "개발 입문하신 초보자 분들 같이 프로젝트 하면서 공부하면서 만들려고 합니다!")
        String content,

        @Schema(description = "프로젝트 사용 기술", example = "[ \"java\", \"kotlin\", \"node.js\", \"python\", \"c\"]")
        List<String> skillSet,

        @Schema(description = "프로젝트 지원 역할(BE or FE)", example = "BE")
        String roleType
) {

        public static Project of(ProjectInfoRequest request, Member member){
                return Project.builder()
                        .fe(request.fe())
                        .be(request.be())
                        .title(request.title())
                        .content(request.content())
                        .state(ProjectState.RECRUIT)
                        .registeredBy(member.getNickName())
                        .recruitDate(request.recruitDate())
                        .startDate(request.startDate())
                        .endDate(request.endDate())
                        .build();
        }

}

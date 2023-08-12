package dev.sodev.domain.project.dto.requset;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.sodev.domain.enums.ProjectRole;
import dev.sodev.domain.enums.ProjectState;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.project.Project;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Schema(description = "Project Feed Request")
public record ProjectInfoRequest(

        @NotNull(message = "인원수를 입력해주세요.")
        @Schema(description = "백엔드 인원수", example = "3")
        Integer be,

        @NotNull(message = "인원수를 입력해주세요.")
        @Schema(description = "프론트엔드 인원수", example = "2")
        Integer fe,

        @NotNull(message = "프로젝트 시작일을 선택해주세요.")
        @Future(message = "기간을 현재보다 과거로 지정할 수 없습니다.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "프로젝트 시작 일시", example = "2023-08-30 09:00:00", type = "string")
        LocalDateTime startDate,

        @NotNull(message = "프로젝트 종료일을 선택해주세요.")
        @Future(message = "기간을 현재보다 과거로 지정할 수 없습니다.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "프로젝트 종료 일시", example = "2023-09-30 18:00:00", type = "string")
        LocalDateTime endDate,

        @NotNull(message = "프로젝트 모집기간을 선택해주세요")
        @Future(message = "기간을 현재보다 과거로 지정할 수 없습니다.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "프로젝트 모집 일시", example = "2023-08-25 00:00:00", type = "string")
        LocalDateTime recruitDate,

        @NotBlank(message = "제목은 공백이 불가합니다.")
        @Schema(description = "프로젝트 제목", example = "간단한 SNS 플랫폼 프로젝트 진행하려고 합니다~")
        String title,

        @NotBlank(message = "내용을 입력해주세요.")
        @Schema(description = "프로젝트 내용", example = "개발 입문하신 초보자 분들 같이 프로젝트 하면서 공부하면서 만들려고 합니다!")
        String content,

        @NotNull(message = "사용 스킬을 최소 한개 이상 입력해주세요.")
        @Schema(description = "프로젝트 사용 기술", example = "[ \"java\", \"kotlin\", \"node.js\", \"python\", \"c\"]")
        List<String> skillSet,

        @NotNull
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

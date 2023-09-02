package dev.sodev.domain.project.dto.requset;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.sodev.domain.enums.ProjectState;
import dev.sodev.domain.enums.SkillCode;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.project.Project;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
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

        public static Project toEntity(ProjectInfoRequest request, Member member){
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

        // 요청 skill 검증.
        public void validateSkills() {
                if (this.skillSet().stream().anyMatch(skill -> SkillCode.findSkillCode(skill) == null)) {
                        throw new SodevApplicationException(ErrorCode.SKILL_NOT_FOUND, "목록에 없는 기술스택입니다.");
                }
        }

        // ProjectInfoRequest 유효성 검증 메서드.
        public void validateUpdateRequest() {
                if (this.title() != null && this.title().trim().isEmpty()) throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "제목은 공백이 불가합니다.");
                if (this.content() != null && this.content().trim().isEmpty()) throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "내용을 입력해주세요.");
                if (this.be() != null && this.be() <= 0) throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "백엔드 인원수는 음수일 수 없습니다.");
                if (this.fe() != null && this.fe() <= 0) throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "프론트엔드 인원수는 음수일 수 없습니다.");
                if (this.startDate() != null && this.startDate().isBefore(LocalDateTime.now())) throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "프로젝트 시작일을 현재보다 과거로 지정할 수 없습니다.");
                if (this.endDate() != null && this.endDate().isBefore(LocalDateTime.now())) throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "프로젝트 종료일을 현재보다 과거로 지정할 수 없습니다.");
                if (this.recruitDate() != null && this.recruitDate().isBefore(LocalDateTime.now())) throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "프로젝트 모집기간을 현재보다 과거로 지정할 수 없습니다.");
        }
}

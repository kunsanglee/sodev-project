package dev.sodev.domain.member.dto;

import dev.sodev.domain.enums.ProjectRole;
import dev.sodev.domain.member.MemberProject;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
@Schema(description = "Member Project Dto")
public record MemberProjectDto(
        @NotNull
        @Schema(description = "프로젝트 id", example = "1L")
        Long projectId,

        @NotNull
        @Schema(description = "회원 id", example = "1L")
        Long memberId,

        @NotNull
        @Schema(description = "회원 닉네임", example = "test닉네임12")
        String nickName,

        @NotNull
        @Schema(description = "프로젝트 지원 역할(BE or FE)", example = "BE")
        ProjectRole role
) {

    public static MemberProjectDto fromEntity(MemberProject memberProject) {
        return MemberProjectDto.builder()
                .projectId(memberProject.getProject().getId())
                .memberId(memberProject.getMember().getId())
                .nickName(memberProject.getMember().getNickName())
                .role(memberProject.getProjectRole())
                .build();
    }

    public void isRoleCreator() {
        if (this.role().getRole().equals(ProjectRole.Role.CREATOR)) {
            throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "프로젝트 주인은 퇴장시킬 수 없습니다.");
        }
    }

}

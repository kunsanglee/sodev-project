package dev.sodev.domain.member.dto;

import dev.sodev.domain.enums.ProjectRole;
import dev.sodev.domain.enums.ProjectState;
import dev.sodev.domain.member.MemberProject;
import lombok.Builder;

@Builder
public record MemberProjectDto(
        Long projectId,
        Long memberId,
        String nickName,
//        ProjectState state
        ProjectRole role
) {

    public static MemberProjectDto of(MemberProject memberProject) {
        return MemberProjectDto.builder()
                .projectId(memberProject.getProject().getId())
                .memberId(memberProject.getMember().getId())
                .nickName(memberProject.getMember().getNickName())
                .role(memberProject.getRole())
                .build();
    }
}

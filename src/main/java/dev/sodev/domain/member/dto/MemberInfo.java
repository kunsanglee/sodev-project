package dev.sodev.domain.member.dto;

import dev.sodev.domain.Images.Images;
import dev.sodev.domain.enums.ProjectState;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.skill.Skill;
import lombok.Builder;

import java.util.List;

@Builder
public record MemberInfo(
        String email,
        String nickName,
        String phone,
        String introduce,
        Images memberImage,
        Integer follower,
        Integer following,
        List<Skill> skills,
        MemberProjectDto currentProject
) {

    public static MemberInfo from(Member member) {

        MemberProjectDto currentProjectDto = member.getMemberProject()
                .stream()
                .filter(mp -> mp.getProject().getState().equals(ProjectState.PROGRESS) || mp.getProject().getState().equals(ProjectState.RECRUIT))
                .findFirst()
                .map(MemberProjectDto::of)
                .orElse(null);

        return MemberInfo.builder()
                .email(member.getEmail())
                .nickName(member.getNickName())
                .phone(member.getPhone())
                .introduce(member.getIntroduce())
                .memberImage(member.getImages())
                .follower(member.getFollowers().size()) // 팔로워, 팔로잉 size 를 통해서 몇명인지만 확인.
                .following(member.getFollowing().size())
                .currentProject(currentProjectDto)
                .build();
    }
}

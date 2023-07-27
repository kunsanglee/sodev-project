package dev.sodev.domain.member.dto;

import dev.sodev.domain.Images.Images;
import dev.sodev.domain.enums.ProjectState;
import dev.sodev.domain.follow.dto.FollowDto;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.project.Project;
import dev.sodev.domain.skill.Skill;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Builder
public record MemberInfo(
        String email,
        String nickName,
        String phone,
        String introduce,
        Images memberImage,
        List<FollowDto> follower,
        List<FollowDto> following,
        List<Skill> skills,
        List<MemberProjectDto> projects,
        List<MemberProjectDto> applyProjects
) {

    public static MemberInfo from(Member member) {

        List<FollowDto> followers = member.getFollowers().stream().map(FollowDto::follower).toList();
        List<FollowDto> following = member.getFollowing().stream().map(FollowDto::following).toList();
        List<MemberProjectDto> memberProjects = member.getMemberProject().stream().map(MemberProjectDto::of).toList();
        List<MemberProjectDto> applyProjects = member.getApplies().stream().map(MemberProjectDto::of).toList();

        return MemberInfo.builder()
                .email(member.getEmail())
                .nickName(member.getNickName())
                .phone(member.getPhone())
                .introduce(member.getIntroduce())
                .memberImage(member.getImages())
                .follower(followers)
                .following(following)
//                .skills() // 구현해서 넣어야함.
                .projects(memberProjects)
                .applyProjects(applyProjects)
                .build();
    }
}

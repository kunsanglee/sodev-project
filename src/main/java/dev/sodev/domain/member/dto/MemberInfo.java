package dev.sodev.domain.member.dto;

import dev.sodev.domain.Images.Images;
import dev.sodev.domain.follow.dto.FollowDto;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.project.Project;
import dev.sodev.domain.skill.Skill;

import java.util.ArrayList;
import java.util.List;

public record MemberInfo(
        String email,
        String nickName,
        String phone,
        String introduce,
        Images memberImage,
        List<FollowDto> follower,
        List<FollowDto> following,
        List<Skill> skills,
        List<Project> projects
) {

    public static MemberInfo from(Member member) {

        List<FollowDto> followers = member.getFollowers().stream().map(follower ->
                FollowDto.builder()
                        .memberId(follower.getFromMember().getId())
                        .email(follower.getFromMember().getEmail())
                        .nickName(follower.getFromMember().getNickName())
                        .build()).toList();

        List<FollowDto> following = member.getFollowing().stream().map(follower ->
                FollowDto.builder()
                        .memberId(follower.getToMember().getId())
                        .email(follower.getToMember().getEmail())
                        .nickName(follower.getToMember().getNickName())
                        .build()).toList();

        return new MemberInfo(
                member.getEmail(),
                member.getNickName(),
                member.getPhone(),
                member.getIntroduce(),
                member.getImages(),
                followers,
                following,
                new ArrayList<Skill>(),
                new ArrayList<Project>()
        );
    }
}

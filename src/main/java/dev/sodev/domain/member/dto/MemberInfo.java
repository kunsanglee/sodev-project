package dev.sodev.domain.member.dto;

import dev.sodev.domain.Images.Images;
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
        Long follower,
        Long following,
        List<Skill> skills,
        List<Project> projects
) {

    public static MemberInfo from(Member member) {
        return new MemberInfo(
                member.getEmail(),
                member.getNickName(),
                member.getPhone(),
                member.getIntroduce(),
                member.getImages(),
                member.getFollower(),
                member.getFollowing(),
                new ArrayList<Skill>(),
                new ArrayList<Project>()
        );
    }
}

package dev.sodev.domain.member.dto;

import dev.sodev.domain.Images.Images;
import dev.sodev.domain.member.Member;

public record MemberInfo(
        String email,
        String nickName,
        String phone,
        String introduce,
        Images memberImage
) {

    public static MemberInfo from(Member member) {
        return new MemberInfo(
                member.getEmail(),
                member.getNickName(),
                member.getPhone(),
                member.getIntroduce(),
                member.getImages()
        );
    }
}

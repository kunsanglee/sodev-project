package dev.sodev.domain.member.dto;

import dev.sodev.domain.Images.Images;
import dev.sodev.domain.member.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MemberInfo(
        @NotBlank(message = "아이디를 입력해주세요.")
        @Email(message = "아이디는 email 형식 이어야 합니다.")
        String email,

        @NotBlank(message = "닉네임을 입력해주세요.")
        String nickName,

        @NotBlank
        @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$",message = "양식에 맞게 입력해주세요. ex)010-1234-5678")
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

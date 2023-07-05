package dev.sodev.domain.member.dto.request;

import dev.sodev.domain.Images.Images;
import dev.sodev.domain.member.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;


@Builder
public record MemberUpdateRequest(
        @NotBlank(message = "아이디를 입력해주세요.")
        @Email(message = "아이디는 email 형식 이어야 합니다.")
        String email,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Pattern(regexp="(?=.*[0-9])(?=.*[a-z])(?=.*\\W)(?=\\S+$).{6,12}",
                message = "비밀번호는 영문자와 숫자, 특수기호가 적어도 1개 이상 포함된 6자~12자의 비밀번호여야 합니다.")
        String password,

        @NotBlank(message = "닉네임을 입력해주세요.")
        String nickName,

        @NotBlank
        @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$",message = "양식에 맞게 입력해주세요. ex)010-1234-5678")
        String phone,

        String introduce,

        Images memberImage
        ) {
}

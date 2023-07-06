package dev.sodev.domain.member.dto.request;

import dev.sodev.domain.Images.Images;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;


@Builder
public record MemberUpdateRequest(
        @NotBlank(message = "아이디를 입력해주세요.")
        @Email(message = "아이디는 email 형식 이어야 합니다.")
        String email,

        @NotBlank(message = "닉네임을 입력해주세요.")
        @Pattern(regexp = "^(?=.*[a-zA-Z0-9가-힣])[a-zA-Z0-9가-힣]{2,16}$", message = "2자 이상 16자 이하, 영어 또는 숫자 또는 한글로 구성")
        String nickName,

        @NotBlank
        @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$",message = "양식에 맞게 입력해주세요. ex)010-1234-5678")
        String phone,

        String introduce,

        Images memberImage
        ) {
}

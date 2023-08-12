package dev.sodev.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
@Schema(description = "Member Login Request")
public record MemberLoginRequest(
        @NotBlank(message = "아이디를 입력해주세요.")
        @Email(message = "아이디는 email 형식 이어야 합니다.")
        @Schema(description = "member email", example = "sodev@sodev.com")
        String email,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Pattern(regexp="(?=.*[0-9])(?=.*[a-z])(?=.*\\W)(?=\\S+$).{6,12}",
                message = "비밀번호는 영문자와 숫자, 특수기호가 적어도 1개 이상 포함된 6자~12자의 비밀번호여야 합니다.")
        @Schema(description = "member password", example = "asdf1234!")
        String password
) {
}

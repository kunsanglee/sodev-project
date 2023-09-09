package dev.sodev.domain.member.dto.request;

import dev.sodev.domain.Images.Images;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;


@Builder
@Schema(description = "Member Update Request")
public record MemberUpdateRequest(

        @Email(message = "아이디는 email 형식 이어야 합니다.")
        @Schema(description = "member email", example = "sodev@sodev.com")
        String email,

        @Pattern(regexp = "^(?=.*[a-zA-Z0-9가-힣])[a-zA-Z0-9가-힣]{2,16}$", message = "2자 이상 16자 이하, 영어 또는 숫자 또는 한글로 구성")
        @Schema(description = "member nickName", example = "test닉네임12")
        String nickName,

        @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$",message = "양식에 맞게 입력해주세요. ex)010-1234-5678")
        @Schema(description = "member phone", example = "010-1234-5678")
        String phone,

        @Schema(description = "member introduce", example = "안녕하세요 간단한 자기소개를 적을 수 있습니다.")
        String introduce,

        Images memberImage
        ) {
}

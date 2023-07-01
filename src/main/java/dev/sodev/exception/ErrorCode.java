package dev.sodev.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부에서 에러가 발생했습니다"),
    DUPLICATE_USER_ID(HttpStatus.CONFLICT, "이미 존재하는 아이디 입니다." ),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰 입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원 입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "일치하지 않는 비밀번호 입니다.")
    ;

    private HttpStatus status;
    private String message;
}

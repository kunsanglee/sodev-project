package dev.sodev.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부에서 에러가 발생했습니다"),
    DUPLICATE_USER_ID(HttpStatus.CONFLICT, "이미 존재하는 아이디 입니다." ),;

    private HttpStatus status;
    private String message;
}

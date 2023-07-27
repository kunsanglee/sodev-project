package dev.sodev.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부에서 에러가 발생했습니다"),
    DUPLICATE_USER_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 아이디 입니다." ),
    DUPLICATE_USER_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 닉네임 입니다." ),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰 입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원 입니다."),
    FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 팔로우 입니다."),
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 이메일 입니다."),
    FEED_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시글 입니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글 입니다."),
    ALREADY_IN_PROJECT(HttpStatus.CONFLICT, "이미 참여중인 프로젝트가 있습니다."),

    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "유효하지 않는 비밀번호 입니다."),
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "로그인 후에 이용하실 수 있습니다."),
    INVALID_PERMISSION(HttpStatus.UNAUTHORIZED, "수정,삭제는 본인이 작성한 글만 가능합니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    ACCESS_FORBIDDEN(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다."),
    ACCESS_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "다시 로그인 한 후에 요청해주세요."),
    BAD_CREDENTIAL(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 맞지 않습니다."),
    WITHDRAWAL_USER(HttpStatus.UNAUTHORIZED, "이미 탈퇴한 회원입니다."),
    ;

    private HttpStatus status;
    private String message;
}

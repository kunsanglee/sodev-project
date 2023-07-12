package dev.sodev.global.exception;

import dev.sodev.global.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(SodevApplicationException.class)
    public ResponseEntity<?> applicationHandler(SodevApplicationException e) {
        log.error("Error occur {}", e.toString());
        return ResponseEntity.status(e.getErrorCode().getStatus())
                .body(Response.error(e.getErrorCode().name()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> applicationHandler(RuntimeException e) {
        log.error("Error occur {}", e.toString());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Response.error(ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> authenticationException(AuthenticationException e) {
        if (e instanceof BadCredentialsException) { // 아이디 또는 비밀번호가 맞지 않습니다. 다시 확인해주세요
            log.error("Error occur {}", e.toString());
            return ResponseEntity.status(ErrorCode.BAD_CREDENTIAL.getStatus().value())
                    .body(Response.error(ErrorCode.BAD_CREDENTIAL.name()));
        } else if (e instanceof UsernameNotFoundException) { // 존재하지 않는 계정입니다. 회원가입 후 로그인해주세요.
            log.error("Error occur {}", e.toString());
            return ResponseEntity.status(ErrorCode.MEMBER_NOT_FOUND.getStatus().value())
                    .body(Response.error(ErrorCode.MEMBER_NOT_FOUND.name()));
        } else if (e instanceof AuthenticationCredentialsNotFoundException) { // 인증 요청이 거부되었습니다. 관리자에게 문의하세요.
            log.error("Error occur {}", e.toString());
            return ResponseEntity.status(ErrorCode.ACCESS_FORBIDDEN.getStatus().value())
                    .body(Response.error(ErrorCode.ACCESS_FORBIDDEN.name()));
        } else {
            log.error("Error occur {}", e.toString());
            return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus().value())
                    .body(Response.error(ErrorCode.INTERNAL_SERVER_ERROR.name()));
        }
    }

    @ExceptionHandler(AuthenticationServiceException.class)
    public ResponseEntity<?> authenticationServiceException(AuthenticationServiceException e) {
        if (e instanceof InternalAuthenticationServiceException) { // 내부 시스템 문제로 로그인 요청을 처리할 수 없습니다. 관리자에게 문의하세요
            log.error("Error occur {}", e.toString());
            return ResponseEntity.status(ErrorCode.EMAIL_NOT_FOUND.getStatus().value())
                    .body(Response.error(ErrorCode.EMAIL_NOT_FOUND.name()));
        } else {
            log.error("Error occur {}", e.toString());
            return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus().value())
                    .body(Response.error(ErrorCode.INTERNAL_SERVER_ERROR.name()));
        }
    }

//    @ExceptionHandler()
}

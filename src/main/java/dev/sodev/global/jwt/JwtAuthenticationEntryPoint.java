package dev.sodev.global.jwt;

import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import dev.sodev.global.redis.CacheName;
import dev.sodev.global.redis.RedisService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final RedisService redisService;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        response.setCharacterEncoding("utf-8");

        if (authException instanceof BadCredentialsException) {
            throw new SodevApplicationException(ErrorCode.INVALID_PASSWORD);
        } else if (authException instanceof UsernameNotFoundException) {
            throw new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND);
        } else if (authException instanceof AuthenticationCredentialsNotFoundException) {
            throw new SodevApplicationException(ErrorCode.INTERNAL_SERVER_ERROR);
        } else {
            response.setCharacterEncoding("utf-8");
            response.sendError(401, "인증에 실패했습니다.");
        }




    }
}
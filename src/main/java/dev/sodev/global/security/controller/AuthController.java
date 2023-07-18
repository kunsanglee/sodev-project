package dev.sodev.global.security.controller;

import dev.sodev.domain.member.dto.MemberWithdrawal;
import dev.sodev.domain.member.dto.request.MemberLoginRequest;
import dev.sodev.global.Response;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.security.dto.JsonWebTokenDto;
import dev.sodev.global.security.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import static dev.sodev.global.security.utils.SecurityUtil.getMemberEmail;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1")
@RestController
public class AuthController {

    private final String AUTHORIZATION_HEADER = "Authorization";
    private final String REFRESH_TOKEN = "refresh-token";
    private final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;

    private final long COOKIE_EXPIRATION = 1209600000; // 2주

    // 로그인 -> 토큰 발급
    @PostMapping("/login")
    public Response<?> login(@RequestBody @Valid MemberLoginRequest request, HttpServletResponse response) {

        JsonWebTokenDto token = authService.login(request);

        ResponseCookie responseCookie = ResponseCookie.from(REFRESH_TOKEN, token.refreshToken())
                .maxAge(COOKIE_EXPIRATION)
                .httpOnly(true)
                .secure(true)
                .build();

        response.addHeader(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token.accessToken()); // access
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

//        return Response.success(new MemberLoginResponse("로그인에 성공하였습니다."));
        return Response.success(token);
    }



    // 토큰 재발급
    @PostMapping("/reissue")
    public Response<?> reissue(@CookieValue(REFRESH_TOKEN) String refreshToken, HttpServletResponse response) {
        JsonWebTokenDto reissuedTokenDto = authService.reissue(refreshToken);

        if (reissuedTokenDto != null) { // 토큰 재발급 성공
            // RT 저장
            ResponseCookie responseCookie = ResponseCookie.from(REFRESH_TOKEN, reissuedTokenDto.refreshToken())
                    .maxAge(COOKIE_EXPIRATION)
                    .httpOnly(true)
                    .secure(true)
                    .build();

            response.addHeader(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + reissuedTokenDto.accessToken());
            response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

            return Response.success(reissuedTokenDto);

        } else { // Refresh Token 탈취 가능성
            // Cookie 삭제 후 재로그인 유도
            ResponseCookie responseCookie = ResponseCookie.from(REFRESH_TOKEN, "")
                    .maxAge(0)
                    .path("/")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.getValue());

            return Response.error(ErrorCode.ACCESS_UNAUTHORIZED.getMessage());
        }
    }

    // 로그아웃
    @PostMapping("/logout")
    public Response<?> logout(@RequestHeader(AUTHORIZATION_HEADER) String accessToken,
                              @CookieValue(REFRESH_TOKEN) String refreshToken,
                              HttpServletResponse response) {

        authService.logout(accessToken, refreshToken);
        ResponseCookie responseCookie = ResponseCookie.from(REFRESH_TOKEN, "")
                .maxAge(0)
                .path("/")
                .build();
        // 쿠키 헤더에 포함하지말고 그냥 쿠키로 보내야될 것 같은데

        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

        return Response.success();
    }

    // 회원 탈퇴
    @DeleteMapping("/members")
    public Response<?> withdrawalMember(@RequestBody @Valid MemberWithdrawal memberWithdrawal,
                                                           @RequestHeader(AUTHORIZATION_HEADER) String accessToken,
                                                           @CookieValue(REFRESH_TOKEN) String refreshToken) {
        authService.withdrawal(memberWithdrawal, accessToken, refreshToken);
        return Response.success();
    }
}

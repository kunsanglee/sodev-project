package dev.sodev.domain.member.controller;

import dev.sodev.domain.member.dto.request.MemberJoinRequest;
import dev.sodev.domain.member.dto.request.MemberLoginRequest;
import dev.sodev.domain.member.dto.request.MemberUpdateRequest;
import dev.sodev.domain.member.dto.response.MemberJoinResponse;
import dev.sodev.domain.member.dto.response.MemberLoginResponse;
import dev.sodev.domain.member.dto.response.MemberUpdateResponse;
import dev.sodev.global.Response;
import dev.sodev.domain.member.dto.MemberInfo;
import dev.sodev.domain.member.dto.MemberWithdrawal;
import dev.sodev.domain.member.dto.UpdatePassword;
import dev.sodev.domain.member.service.MemberService;
import dev.sodev.global.jwt.TokenDto;
import dev.sodev.global.jwt.AuthService;
import dev.sodev.global.util.SecurityUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/v1")
@RestController
public class MemberController {

    private final MemberService memberService;
    private final AuthService authService;

    private final long COOKIE_EXPIRATION = 7776000; // 90일

    @PostMapping("/join")
    public Response<MemberJoinResponse> join(@RequestBody @Valid MemberJoinRequest request) {
        MemberJoinResponse response = memberService.join(request);
        return Response.success(response);
    }

    // 로그인 -> 토큰 발급
    @PostMapping("/login")
    public Response<MemberLoginResponse> login(@RequestBody @Valid MemberLoginRequest request, HttpServletResponse response) {
        // User 등록 및 Refresh Token 저장
        TokenDto tokenDto = authService.login(request);

        // RT 저장
        HttpCookie httpCookie = ResponseCookie.from("refresh-token", tokenDto.refreshToken())
                .maxAge(COOKIE_EXPIRATION)
                .httpOnly(true)
                .secure(true)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, httpCookie.toString());
        // AT 저장
        response.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tokenDto.accessToken());

        return Response.success(new MemberLoginResponse("로그인에 성공하였습니다."));
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validate(@RequestHeader("Authorization") String requestAccessToken) {
        if (!authService.validate(requestAccessToken)) {
            return ResponseEntity.status(HttpStatus.OK).build(); // 재발급 필요X
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 재발급 필요
        }
    }

    // 토큰 재발급
    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(@RequestHeader("Authorization") String requestAccessToken,
                                     @CookieValue(name = "refresh-token") String requestRefreshToken) {
        TokenDto reissuedTokenDto = authService.reissue(requestAccessToken, requestRefreshToken, SecurityUtil.getMemberEmail());

        if (reissuedTokenDto != null) { // 토큰 재발급 성공
            // RT 저장
            ResponseCookie responseCookie = ResponseCookie.from("refresh-token", reissuedTokenDto.refreshToken())
                    .maxAge(COOKIE_EXPIRATION)
                    .httpOnly(true)
                    .secure(true)
                    .build();

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                    // AT 저장
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + reissuedTokenDto.accessToken())
                    .build();

        } else { // Refresh Token 탈취 가능성
            // Cookie 삭제 후 재로그인 유도
            ResponseCookie responseCookie = ResponseCookie.from("refresh-token", "")
                    .maxAge(0)
                    .path("/")
                    .build();
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                    .build();
        }
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String requestAccessToken, @RequestHeader("Cookie") String requestRefreshToken) {
        authService.logout(requestAccessToken, requestRefreshToken, SecurityUtil.getMemberEmail());
        ResponseCookie responseCookie = ResponseCookie.from("refresh-token", "")
                .maxAge(0)
                .path("/")
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .build();
    }

    @GetMapping("/members/{id}")
    public Response<MemberInfo> memberInfo(@PathVariable Long id) {
        MemberInfo memberInfo = memberService.getMemberInfo(id, SecurityUtil.getMemberEmail());
        return Response.success(memberInfo);
    }

    @GetMapping("/members")
    public Response<MemberInfo> myInfo() {
        MemberInfo memberInfo = memberService.getMyInfo(SecurityUtil.getMemberEmail());
        return Response.success(memberInfo);
    }

    @PatchMapping("/members")
    public Response<MemberUpdateResponse> updateInfo(@RequestBody @Valid MemberUpdateRequest request) {
        MemberUpdateResponse response = memberService.update(request);
        return Response.success(response);
    }

    @PatchMapping("/members/password")
    public Response<MemberUpdateResponse> updatePassword(@RequestBody @Valid UpdatePassword updatePassword) {
        MemberUpdateResponse response = memberService.updatePassword(updatePassword, SecurityUtil.getMemberEmail());
        return Response.success(response);
    }

    @DeleteMapping("/members")
    public Response<MemberUpdateResponse> withdrawalMember(@RequestBody @Valid MemberWithdrawal memberWithdrawal,
                                                           @RequestHeader("Authorization") String requestAccessToken,
                                                           @RequestHeader("Cookie") String requestRefreshToken) {
        authService.logout(requestAccessToken, requestRefreshToken, SecurityUtil.getMemberEmail());
        MemberUpdateResponse response = memberService.withdrawal(memberWithdrawal, SecurityUtil.getMemberEmail());
        return Response.success(response);
    }
}

package dev.sodev.global.security.service;

import dev.sodev.domain.enums.Auth;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.member.dto.request.MemberLoginRequest;
import dev.sodev.domain.member.repository.MemberRepository;
import dev.sodev.global.redis.RedisService;
import dev.sodev.global.security.dto.JsonWebTokenDto;
import dev.sodev.global.security.exception.JwtInvalidException;
import dev.sodev.global.security.utils.JsonWebTokenIssuer;
import dev.sodev.global.security.utils.SecurityUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class AuthServiceTest {

    MemberRepository mockAuthRepository;
    PasswordEncoder passwordEncoder;
    JsonWebTokenIssuer mockJwtIssuer;
    RedisService redisService;
    AuthService authService;

    @BeforeEach
    public void setup() {
        mockAuthRepository = Mockito.mock(MemberRepository.class);
        passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        mockJwtIssuer = Mockito.mock(JsonWebTokenIssuer.class);
        redisService = Mockito.mock(RedisService.class);
        authService = new AuthService(mockAuthRepository, passwordEncoder, mockJwtIssuer, redisService);
    }

    MemberLoginRequest getMemberLoginRequest(String memberEmail, String password) {
        return MemberLoginRequest.builder()
                .email(memberEmail)
                .password(password)
                .build();
    }

    Member getMember(String memberEmail, String password, Auth authority) {
        return Member.builder()
                .email(memberEmail)
                .password(passwordEncoder.encode(password))
                .auth(authority)
                .build();
    }

    @Test
    public void givenNotExistUserName_whenLogin_thenThrowUsernameNotFoundException() {

        MemberLoginRequest request = getMemberLoginRequest("lee@naver.com", "1234");

        Throwable throwable = assertThrows(UsernameNotFoundException.class, () -> {
            authService.login(request);
        });

        assertThat(throwable, isA(UsernameNotFoundException.class));
        assertThat(throwable.getMessage(), equalTo("username is not found"));
    }

    @Test
    public void givenNotMatchedPassword_whenLogin_thenThrowBadCredentialsException() {

        MemberLoginRequest request = getMemberLoginRequest("lee@naver.com", "1234");
        when(mockAuthRepository.findByEmail("lee@naver.com")).thenReturn(
                Optional.of(
                        getMember("lee@naver.com", "12345", Auth.ADMIN)
                )
        );

        Throwable throwable = assertThrows(BadCredentialsException.class, () -> {
            authService.login(request);
        });

        assertThat(throwable, isA(BadCredentialsException.class));
        assertThat(throwable.getMessage(), equalTo("bad credential: using unmatched password"));
    }

    @Test
    public void givenValidUserDto_whenLogin_thenReturnJsonWebTokenDto() {

        MemberLoginRequest request = getMemberLoginRequest("lee@naver.com", "asdf1234!");
        Member member = getMember("lee@naver.com", "asdf1234!", Auth.ADMIN);
        when(mockAuthRepository.findByEmail("lee@naver.com")).thenReturn(Optional.of(member));
        when(mockJwtIssuer.createAccessToken("lee@naver.com", Auth.ADMIN.getKey())).thenReturn("accessToken");
        when(mockJwtIssuer.createRefreshToken("lee@naver.com", Auth.ADMIN.getKey())).thenReturn("refreshToken");

        JsonWebTokenDto jsonWebTokenDto = authService.login(request);

        assertThat(jsonWebTokenDto.grantType(), equalTo("Bearer"));
        assertThat(jsonWebTokenDto.accessToken(), equalTo("accessToken"));
        assertThat(jsonWebTokenDto.refreshToken(), equalTo("refreshToken"));
    }

    @Test
    public void givenInvalidGrandType_whenReissue_thenThrowJwtInvalidException() {

        Throwable throwable = assertThrows(JwtInvalidException.class, () -> {
            authService.reissue("refreshToken");
        });
        assertThat(throwable.getMessage(), equalTo("not exists claims in token"));
    }

    @Test
    public void givenNullClaims_whenReissue_thenThrowJwtInvalidException() {

        when(mockJwtIssuer.parseClaimsFromRefreshToken("refreshToken")).thenReturn(null);

        Throwable throwable = assertThrows(JwtInvalidException.class, () -> {
            authService.reissue("refreshToken");
        });
        assertThat(throwable.getMessage(), equalTo("not exists claims in token"));
    }

    @Test
    public void givenValidRefreshToken_whenReissue_thenJsonWebTokenDto() {

        Member member = getMember("lee@naver.com", "asdf1234!", Auth.ADMIN);
        Claims claims = Jwts.claims().setSubject("lee@naver.com").setExpiration(new Date());
        claims.put("roles", Collections.singleton("ROLE_ADMIN"));

        when(mockAuthRepository.findByEmail("lee@naver.com")).thenReturn(Optional.of(member));
        when(mockJwtIssuer.parseClaimsFromRefreshToken("refreshToken")).thenReturn(claims);
        when(mockJwtIssuer.createAccessToken("lee@naver.com", Auth.ADMIN.getKey())).thenReturn("accessToken");
        when(mockJwtIssuer.createRefreshToken("lee@naver.com", Auth.ADMIN.getKey())).thenReturn("refreshToken");

        JsonWebTokenDto jsonWebTokenDto = authService.reissue("refreshToken");

        assertThat(jsonWebTokenDto.grantType(), equalTo("Bearer"));
        assertThat(jsonWebTokenDto.accessToken(), equalTo("accessToken"));
        assertThat(jsonWebTokenDto.refreshToken(), equalTo("refreshToken"));
    }
}
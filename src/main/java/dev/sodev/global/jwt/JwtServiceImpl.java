package dev.sodev.global.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sodev.domain.member.Member;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import dev.sodev.domain.member.repository.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Setter(value = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Transactional
@Service
public class JwtServiceImpl implements JwtService{

    //== 1 ==//
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.access.expiration}")
    private long accessTokenValidityInSeconds;
    @Value("${jwt.refresh.expiration}")
    private long refreshTokenValidityInSeconds;
    @Value("${jwt.access.header}")
    private String accessHeader;
    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    //== 2 ==//
    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String MEMBER_EMAIL_CLAIM = "email";
    private static final String BEARER = "Bearer ";


    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    //== 3 ==//
    @Override
    public String createAccessToken(String email) {
        return JWT.create() // JWT 토큰을 생성하는 빌더를 반환합니다.
                .withSubject(ACCESS_TOKEN_SUBJECT) // 빌더를 통해 JWT의 Subject를 정합니다. AccessToken이므로 저는 위에서 설정했던 AccessToken의 subject를 가져와 사용.
                .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenValidityInSeconds * 1000)) // 만료시간 설정 현재시간으로부터 설정한 시간만큼.
                .withClaim(MEMBER_EMAIL_CLAIM, email) // 클레임으로는 저희는 username 하나만 사용합니다. 추가적으로 식별자나, 이름 등의 정보를 더 추가하셔도 됩니다. 추가하실 경우 .withClaim(클래임 이름, 클래임 값) 으로 설정해주시면 됩니다
                .sign(Algorithm.HMAC512(secret)); // HMAC512 알고리즘을 사용하여, 저희가 지정한 secret 키로 암호화 할 것입니다.
    }

    @Override
    public String createRefreshToken() {
        return JWT.create()
                .withSubject(REFRESH_TOKEN_SUBJECT)
                .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenValidityInSeconds * 1000))
                .sign(Algorithm.HMAC512(secret));
    }

    @Override
    public void updateRefreshToken(String email, String refreshToken) {
        memberRepository.findByEmail(email)
                .ifPresentOrElse(
                        member -> member.updateRefreshToken(refreshToken),
                        () -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND, "회원이 없습니다")
                );
    }

    @Override
    public void destroyRefreshToken(String email) {
        memberRepository.findByEmail(email)
                .ifPresentOrElse(
                        Member::destroyRefreshToken,
                        () -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND, "회원이 없습니다")
                );
    }

    //== 5 ==//
    @Override
    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken){
        response.setStatus(HttpServletResponse.SC_OK);

        setAccessTokenHeader(response, accessToken);
        setRefreshTokenHeader(response, refreshToken);

        /*Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put(ACCESS_TOKEN_SUBJECT, accessToken);
        tokenMap.put(REFRESH_TOKEN_SUBJECT, refreshToken);*/ // token 을 헤더에만 보내기 위해 바디에 넣는것 주석

    }

    @Override
    public void sendAccessToken(HttpServletResponse response, String accessToken){
        response.setStatus(HttpServletResponse.SC_OK);

        setAccessTokenHeader(response, accessToken);

        /*Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put(ACCESS_TOKEN_SUBJECT, accessToken);*/ // token 을 헤더에만 보내기 위해 바디에 넣는것 주석
    }

    @Override
    public Optional<String> extractAccessToken(HttpServletRequest request) throws IOException, ServletException {
        return Optional.ofNullable(request.getHeader(accessHeader))
                .filter(accessToken -> accessToken.startsWith(BEARER))
                .map(accessToken -> accessToken.replace(BEARER, ""));
    }

    @Override
    public Optional<String> extractRefreshToken(HttpServletRequest request) throws IOException, ServletException {
        return Optional.ofNullable(request.getHeader(refreshHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, ""));
    }

    //== 4 ==//
    @Override
    public Optional<String> extractEmail(String accessToken) {
        try {
            return Optional.ofNullable(JWT.require(Algorithm.HMAC512(secret)) // 토큰의 서명의 유효성을 검사하는데 사용할 알고리즘이 있는 JWT verifier builder를 반환합니다.
                    .build() // 반환된 빌더로 JWT verifier를 생성합니다
                    .verify(accessToken) // accessToken을 검증하고 유효하지 않다면 예외를 발생시킵니다.
                    .getClaim(MEMBER_EMAIL_CLAIM).asString()); // claim을 가져옵니다
        } catch (Exception e) {
            log.error(e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
        response.setHeader(accessHeader, accessToken);
    }

    @Override
    public void setRefreshTokenHeader(HttpServletResponse response, String refreshToken) {
        response.setHeader(refreshHeader, refreshToken);
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            JWT.require(Algorithm.HMAC512(secret)).build().verify(token);
            return true;
        }catch (Exception e){
            log.error("유효하지 않은 Token입니다", e.getMessage());
            return false;
        }
    }
}


package dev.sodev.global.jwt.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sodev.domain.entity.Member;
import dev.sodev.domain.enums.Auth;
import dev.sodev.global.jwt.JwtService;
import dev.sodev.global.jwt.JwtServiceImpl;
import dev.sodev.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class JwtServiceTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    JwtService jwtService = new JwtServiceImpl(memberRepository, objectMapper);
    @Autowired
    EntityManager em;

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.access.header}")
    private String accessHeader;
    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String MEMBER_CLAIM = "email";
    private static final String BEARER = "Bearer ";


    private String memberEmail = "sodev@sodev.com";

    @BeforeEach
    public void init(){
        Member member = Member.builder().email(memberEmail).password("1234567890").nickName("NickName1").auth(Auth.MEMBER).build();
        memberRepository.save(member);
        clear();
    }

    private void clear(){
        em.flush();
        em.clear();
    }

    private DecodedJWT getVerify(String token) {
        return JWT.require(HMAC512(secret)).build().verify(token);
    }

    @Test
    public void createAccessToken_AccessToken_발급() throws Exception {
        //given, when
        String accessToken = jwtService.createAccessToken(memberEmail);

        DecodedJWT verify = getVerify(accessToken);

        String subject = verify.getSubject();
        String findEmail = verify.getClaim(MEMBER_CLAIM).asString();

        //then
        assertThat(findEmail).isEqualTo(memberEmail);
        assertThat(subject).isEqualTo(ACCESS_TOKEN_SUBJECT);
    }

    @Test
    public void createRefreshToken_RefreshToken_발급() throws Exception {
        //given, when
        String refreshToken = jwtService.createRefreshToken();
        DecodedJWT verify = getVerify(refreshToken);
        String subject = verify.getSubject();
        String findEmail = verify.getClaim(MEMBER_CLAIM).asString();

        //then
        assertThat(subject).isEqualTo(REFRESH_TOKEN_SUBJECT);
        assertThat(findEmail).isNull();
    }

    @Test
    public void updateRefreshToken_refreshToken_업데이트() throws Exception {
        //given
        String refreshToken = jwtService.createRefreshToken();
        jwtService.updateRefreshToken(memberEmail, refreshToken);
        clear();
        Thread.sleep(3000);

        //when
        String reIssuedRefreshToken = jwtService.createRefreshToken();
        jwtService.updateRefreshToken(memberEmail, reIssuedRefreshToken);
        clear();

        //then
        assertThrows(Exception.class, () -> memberRepository.findByRefreshToken(refreshToken).get());//
        assertThat(memberRepository.findByRefreshToken(reIssuedRefreshToken).get().getEmail()).isEqualTo(memberEmail);
    }

    @Test
    public void destroyRefreshToken_refreshToken_제거() throws Exception {
        //given
        String refreshToken = jwtService.createRefreshToken();
        jwtService.updateRefreshToken(memberEmail, refreshToken);
        clear();

        //when
        jwtService.destroyRefreshToken(memberEmail);
        clear();

        //then
        assertThrows(Exception.class, () -> memberRepository.findByRefreshToken(refreshToken).get());

        Member member = memberRepository.findByEmail(memberEmail).get();
        assertThat(member.getRefreshToken()).isNull();
    }

    @Test
    public void setAccessTokenHeader_AccessToken_헤더_설정() throws Exception {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        String accessToken = jwtService.createAccessToken(memberEmail);
        String refreshToken = jwtService.createRefreshToken();

        jwtService.setAccessTokenHeader(mockHttpServletResponse, accessToken);


        //when
        jwtService.sendAccessToken(mockHttpServletResponse,accessToken);

        //then
        String headerAccessToken = mockHttpServletResponse.getHeader(accessHeader);

        assertThat(headerAccessToken).isEqualTo(accessToken);
    }

    @Test
    public void setRefreshTokenHeader_RefreshToken_헤더_설정() throws Exception {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        String accessToken = jwtService.createAccessToken(memberEmail);
        String refreshToken = jwtService.createRefreshToken();

        jwtService.setRefreshTokenHeader(mockHttpServletResponse, refreshToken);


        //when
        jwtService.sendAccessAndRefreshToken(mockHttpServletResponse,accessToken,refreshToken);

        //then
        String headerRefreshToken = mockHttpServletResponse.getHeader(refreshHeader);

        assertThat(headerRefreshToken).isEqualTo(refreshToken);
    }

    @Test
    public void sendAccessAndRefreshToken_토큰_전송() throws Exception {
        //given
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        String accessToken = jwtService.createAccessToken(memberEmail);
        String refreshToken = jwtService.createRefreshToken();


        //when
        jwtService.sendAccessAndRefreshToken(mockHttpServletResponse,accessToken,refreshToken);


        //then
        String headerAccessToken = mockHttpServletResponse.getHeader(accessHeader);
        String headerRefreshToken = mockHttpServletResponse.getHeader(refreshHeader);



        assertThat(headerAccessToken).isEqualTo(accessToken);
        assertThat(headerRefreshToken).isEqualTo(refreshToken);

    }


    @Test
    public void extractAccessToken_AccessToken_추출() throws Exception {
        //given
        String accessToken = jwtService.createAccessToken(memberEmail);
        String refreshToken = jwtService.createRefreshToken();
        HttpServletRequest httpServletRequest = setRequest(accessToken, refreshToken);

        //when
        String extractAccessToken = jwtService.extractAccessToken(httpServletRequest).orElseThrow(()-> new Exception("토큰이 없습니다"));


        //then
        assertThat(extractAccessToken).isEqualTo(accessToken);
        assertThat(getVerify(extractAccessToken).getClaim(MEMBER_CLAIM).asString()).isEqualTo(memberEmail);
    }


    @Test
    public void extractRefreshToken_RefreshToken_추출() throws Exception {
        //given
        String accessToken = jwtService.createAccessToken(memberEmail);
        String refreshToken = jwtService.createRefreshToken();
        HttpServletRequest httpServletRequest = setRequest(accessToken, refreshToken);


        //when
        String extractRefreshToken = jwtService.extractRefreshToken(httpServletRequest).orElseThrow(()-> new Exception("토큰이 없습니다"));


        //then
        assertThat(extractRefreshToken).isEqualTo(refreshToken);
        assertThat(getVerify(extractRefreshToken).getSubject()).isEqualTo(REFRESH_TOKEN_SUBJECT);
    }


    private HttpServletRequest setRequest(String accessToken, String refreshToken) throws IOException {

        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        jwtService.sendAccessAndRefreshToken(mockHttpServletResponse,accessToken,refreshToken);

        String headerAccessToken = mockHttpServletResponse.getHeader(accessHeader);
        String headerRefreshToken = mockHttpServletResponse.getHeader(refreshHeader);

        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();

        httpServletRequest.addHeader(accessHeader, BEARER+headerAccessToken);
        httpServletRequest.addHeader(refreshHeader, BEARER+headerRefreshToken);

        return httpServletRequest;
    }


    @Test
    public void extractUsername_Username_추출() throws Exception {
        //given
        String accessToken = jwtService.createAccessToken(memberEmail);
        String refreshToken = jwtService.createRefreshToken();
        HttpServletRequest httpServletRequest = setRequest(accessToken, refreshToken);

        String requestAccessToken = jwtService.extractAccessToken(httpServletRequest).orElseThrow(()->new Exception("토큰이 없습니다"));

        //when
        String extractUsername = jwtService.extractEmail(requestAccessToken).orElseThrow(()->new Exception("토큰이 없습니다"));


        //then
        assertThat(extractUsername).isEqualTo(memberEmail);
    }
}
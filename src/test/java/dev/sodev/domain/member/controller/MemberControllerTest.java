package dev.sodev.domain.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sodev.domain.member.dto.MemberWithdrawal;
import dev.sodev.domain.member.dto.UpdatePassword;
import dev.sodev.domain.member.dto.request.MemberJoinRequest;
import dev.sodev.domain.member.dto.request.MemberLoginRequest;
import dev.sodev.domain.member.dto.request.MemberUpdateRequest;
import dev.sodev.domain.member.repository.MemberRepository;
import dev.sodev.domain.member.service.MemberService;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import dev.sodev.global.redis.RedisService;
import dev.sodev.global.security.dto.JsonWebTokenDto;
import dev.sodev.global.security.service.AuthService;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MemberControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired ObjectMapper objectMapper;
    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired AuthService authService;
    @Autowired RedisService redisService;

    private static String SIGN_UP_URL = "/v1/join";
    private static String LOGIN_URL = "/v1/login";

    private String EMAIL = "sodev@sodev.com";
    private String PASSWORD = "asdf1234!";
    private String NICKNAME = "testNick";
    private String PHONE = "010-1234-1234";
    private static final String BEARER = "Bearer ";

    private void clear() {
        em.flush();
        em.clear();
    }

    @BeforeEach
    public void setup() {
        clear();
        redisService.delete("login_member::" + EMAIL);
        SecurityContextHolder.getContext().setAuthentication(getAuthenticatedUser());
    }

    private UsernamePasswordAuthenticationToken getAuthenticatedUser() {
        User user = new User("username", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private void join(MemberJoinRequest request) throws Exception {
        mockMvc.perform(
                        post(SIGN_UP_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }


    private JsonWebTokenDto getToken() throws Exception {
        MemberLoginRequest request = MemberLoginRequest.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .build();

        MvcResult result = mockMvc.perform(
                        post(LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()).andReturn();
        String refresh = result.getResponse().getCookie("refresh-token").getValue();
        String access = result.getResponse().getHeader("Authorization");
        return JsonWebTokenDto.builder().accessToken(access).refreshToken(refresh).build();
    }

    private void joinDefault() throws Exception {
        MemberJoinRequest joinData = MemberJoinRequest.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .nickName(NICKNAME)
                .phone(PHONE)
                .build();
        join(joinData);
    }

    @Test
    public void 회원가입_성공() throws Exception {
        assertDoesNotThrow(this::joinDefault);
    }

    @Test
    public void 회원가입_이메일_중복_실패() throws Exception {
        //given
        joinDefault(); // 먼저 가입한 회원
        MemberJoinRequest joinData = MemberJoinRequest.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .nickName(NICKNAME +1)
                .phone(PHONE)
                .build();

        //when
        mockMvc.perform(
                        post(SIGN_UP_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(joinData)))
                .andExpect(status().is(ErrorCode.DUPLICATE_USER_EMAIL.getStatus().value())); // 이메일 중복에러

        //then
        assertThat(memberRepository.findAll().size()).isEqualTo(1); // 등록된 회원은 먼저 가입한 1명
    }

    @Test
    public void 회원가입_닉네임_중복_실패() throws Exception {
        //given
        joinDefault(); // 먼저 가입한 회원
        MemberJoinRequest joinData = MemberJoinRequest.builder()
                .email(1+ EMAIL)
                .password(PASSWORD)
                .nickName(NICKNAME)
                .phone(PHONE)
                .build();

        //when
        mockMvc.perform(
                        post(SIGN_UP_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(joinData)))
                .andExpect(status().is(ErrorCode.DUPLICATE_USER_NICKNAME.getStatus().value())); // 이메일은 다르지만 닉네임 중복에러

        //then
        assertThat(memberRepository.findAll().size()).isEqualTo(1); // 등록된 회원은 먼저 가입한 1명
    }

//    @WithMockUser
    @Test
    public void 회원정보수정_성공() throws Exception {
        //given
        joinDefault();
        JsonWebTokenDto token = getToken();
        String accessToken = token.accessToken();
        String refreshToken = token.refreshToken();
        MemberUpdateRequest request = MemberUpdateRequest.builder()
                .email(EMAIL)
                .nickName("change")
                .phone("010-1111-2222")
                .introduce("테스트 자기소개 수정")
                .build();

        //when
        MvcResult result = mockMvc.perform(
                        patch("/v1/members")
                                .header("Authorization", accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()).andReturn();

        //then
//        Member updatedMember = memberRepository.findByEmail(EMAIL).orElseThrow();
//        assertThat(updatedMember.getNickName()).isEqualTo("change");
//        assertThat(updatedMember.getPhone()).isEqualTo("010-1111-2222");
//        assertThat(updatedMember.getIntroduce()).isEqualTo("테스트 자기소개 수정");
    }

    @Test
    public void 회원정보_수정시_닉네임_중복_실패() throws Exception {
        //given
        MemberJoinRequest joinData = MemberJoinRequest.builder()
                .email("test"+ EMAIL)
                .password(PASSWORD)
                .nickName("test"+ NICKNAME)
                .phone(PHONE)
                .build();
        join(joinData);


        joinDefault();
        JsonWebTokenDto token = getToken();
        String accessToken = token.accessToken();
        String refreshToken = token.refreshToken();
        MemberUpdateRequest request = MemberUpdateRequest.builder()
                .email(EMAIL)
                .nickName("test"+ NICKNAME)
                .phone("010-1111-2222")
                .introduce("테스트 자기소개 수정")
                .build();

        //when & then
        MvcResult result = mockMvc.perform(
                        patch("/v1/members")
                                .header("Authorization", accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(ErrorCode.DUPLICATE_USER_NICKNAME.getStatus().value())).andReturn();
    }

    @WithMockUser
    @Test
    public void 비밀번호수정_성공() throws Exception {
        //given
        joinDefault();
        JsonWebTokenDto token = getToken();
        String accessToken = token.accessToken();
        String refreshToken = token.refreshToken();
        UpdatePassword request = UpdatePassword.builder()
                .checkPassword(PASSWORD)
                .toBePassword("1234asdf!")
                .build();

        //when
        MvcResult result = mockMvc.perform(
                        patch("/v1/members/password")
                                .header("Authorization", accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()).andReturn();

        //then
//        Member updatedMember = memberRepository.findByEmail(EMAIL).orElseThrow();
//        assertThat(passwordEncoder.matches("1234asdf!", updatedMember.getPassword())).isTrue();
    }

    @Test
    public void 비밀번호수정_새로운_비밀번호_검증_에러() throws Exception {
        //given
        joinDefault();
        JsonWebTokenDto token = getToken();
        String accessToken = token.accessToken();
        String refreshToken = token.refreshToken();
        UpdatePassword request = UpdatePassword.builder()
                .checkPassword(PASSWORD)
                .toBePassword("123456")
                .build();

        //when
        MvcResult result = mockMvc.perform(
                        patch("/v1/members/password")
                                .header("Authorization", accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()).andReturn();

        //then
        String content = result.getResponse().getContentAsString();
        assertThat(content.contains("validation error")).isTrue();
        assertThat(content.contains("비밀번호는 영문자와 숫자, 특수기호가 적어도 1개 이상 포함된 6자~12자의 비밀번호여야 합니다.")).isTrue();
    }

    @Test
    public void 비밀번호수정_변경_전_비밀번호_검증_에러() throws Exception {
        //given
        joinDefault();
        JsonWebTokenDto token = getToken();
        String accessToken = token.accessToken();
        String refreshToken = token.refreshToken();
        UpdatePassword request = UpdatePassword.builder()
                .checkPassword(PASSWORD +1)
                .toBePassword("1234asdf!")
                .build();

        //when & then
        MvcResult result = mockMvc.perform(
                        patch("/v1/members/password")
                                .header("Authorization", accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(ErrorCode.INVALID_PASSWORD.getStatus().value())).andReturn();
    }

    @Test
    public void 회원탈퇴_성공() throws Exception {
        //given
        joinDefault();
        JsonWebTokenDto token = getToken();
        String accessToken = token.accessToken();
        String refreshToken = token.refreshToken();
        MemberWithdrawal request = MemberWithdrawal.builder()
                .checkPassword(PASSWORD)
                .build();

        //when
        MvcResult result = mockMvc.perform(
                        delete("/v1/members")
                                .header("Authorization", accessToken)
                                .cookie(new Cookie("refresh-token", refreshToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()).andReturn();
        //then
        assertThrows(SodevApplicationException.class,
                () -> memberRepository.findByEmail(EMAIL).orElseThrow(
                        () -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND)));
    }

    @Test
    public void 회원탈퇴_비밀번호_검증_실패() throws Exception {
        //given
        joinDefault();
        JsonWebTokenDto token = getToken();
        String accessToken = token.accessToken();
        String refreshToken = token.refreshToken();
        MemberWithdrawal request = MemberWithdrawal.builder()
                .checkPassword(PASSWORD +1) // 틀린 확인 비밀번호
                .build();

        //when & then
        MvcResult result = mockMvc.perform(
                        delete("/v1/members")
                                .header("Authorization", accessToken)
                                .cookie(new Cookie("refresh-token", refreshToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(ErrorCode.INVALID_PASSWORD.getStatus().value())).andReturn();
    }

    @Test
    public void 회원탈퇴_엑세스토큰_틀려서_실패() throws Exception {
        //given
        joinDefault();
        JsonWebTokenDto token = getToken();
        String accessToken = token.accessToken();
        String refreshToken = token.refreshToken();
        MemberWithdrawal request = MemberWithdrawal.builder()
                .checkPassword(PASSWORD) // 틀린 확인 비밀번호
                .build();

        //when & then
        MvcResult result = mockMvc.perform(
                        delete("/v1/members")
                                .header("Authorization", accessToken)
                                .cookie(new Cookie("refresh-token", "INVALID-TOKEN123231123312123"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError()).andReturn();
        log.info("result={}", result.getResponse().getContentAsString());
    }

}

package dev.sodev.domain.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.member.dto.request.MemberJoinRequest;
import dev.sodev.domain.member.dto.request.MemberUpdateRequest;
import dev.sodev.domain.member.repository.MemberRepository;
import dev.sodev.domain.member.service.MemberService;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;


import java.util.HashMap;
import java.util.Map;

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

    private static String SIGN_UP_URL = "/v1/join";

    private String email = "sodev@sodev.com";
    private String password = "asdf1234!";
    private String nickName = "testNick";
    private String phone = "010-1234-1234";

    private void clear() {
        em.flush();
        em.clear();
    }

    private void join(String joinData) throws Exception {
        mockMvc.perform(
                        post(SIGN_UP_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(joinData))
                .andExpect(status().isOk());
    }

    @Value("${jwt.access.header}")
    private String accessHeader;

    private static final String BEARER = "Bearer ";

    private String getAccessToken() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("email", email);
        map.put("password", password);

        MvcResult result = mockMvc.perform(
                        post("/v1/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(map)))
                .andExpect(status().isOk()).andReturn();

        return result.getResponse().getHeader(accessHeader);
    }

    @Test
    public void 회원가입_성공() throws Exception {
        //given
        String joinData = objectMapper.writeValueAsString(new MemberJoinRequest(email, password, nickName, phone));

        //when
        join(joinData);

        //then
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
        assertThat(member.getEmail()).isEqualTo("sodev@sodev.com");
        assertThat(memberRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    public void 회원가입_이메일_중복_실패() throws Exception {
        //given
        String joinData = objectMapper.writeValueAsString(new MemberJoinRequest(email, password, nickName, phone));

        //when
        join(joinData);

        String joinData2 = objectMapper.writeValueAsString(new MemberJoinRequest(email, password, nickName + "asd", phone));
        //then
        mockMvc.perform(
                        post("/v1/join")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(joinData2))
                .andExpect(status().is(new SodevApplicationException(ErrorCode.DUPLICATE_USER_ID).getErrorCode().getStatus().value()));
    }

    @Test
    public void 회원정보수정_성공() throws Exception {
        //given
        String joinData = objectMapper.writeValueAsString(new MemberJoinRequest(email, password, nickName, phone));
        join(joinData);
        String accessToken = getAccessToken();

        String updateMemberData = objectMapper.writeValueAsString(
                new MemberUpdateRequest(
                        "sodev@sodev.com",
                        "change",
                        "010-1111-2222",
                        null,
                        null)
        );


        //when
        mockMvc.perform(
                        patch("/v1/members")
                                .header(accessHeader, BEARER + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateMemberData))
                .andExpect(status().isOk());

        //then
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
        assertThat(member.getNickName()).isEqualTo("change");
        assertThat(member.getPhone()).isEqualTo("010-1111-2222");
        assertThat(memberRepository.findAll().size()).isEqualTo(1);

    }

    @Test
    public void 비밀번호수정_성공() throws Exception {
        //given
        String joinData = objectMapper.writeValueAsString(new MemberJoinRequest(email, password, nickName, phone));
        join(joinData);

        String accessToken = getAccessToken();

        Map<String, Object> map = new HashMap<>();
        map.put("checkPassword", password);
        map.put("toBePassword", password + "!@#");

        String updatePassword = objectMapper.writeValueAsString(map);


        //when
        mockMvc.perform(
                        patch("/v1/members/password")
                                .header(accessHeader, BEARER + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updatePassword))
                .andExpect(status().isOk());

        //then
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
        assertThat(passwordEncoder.matches(password, member.getPassword())).isFalse();
        assertThat(passwordEncoder.matches(password + "!@#", member.getPassword())).isTrue();
    }

    @Test
    public void 비밀번호수정_새로운_비밀번호_검증_에러() throws Exception {
        //given
        String joinData = objectMapper.writeValueAsString(new MemberJoinRequest(email, password, nickName, phone));
        join(joinData);

        String accessToken = getAccessToken();

        Map<String, Object> map = new HashMap<>();
        map.put("checkPassword", password);
        map.put("toBePassword", password + "!@#!@$%@%%@");

        String updatePassword = objectMapper.writeValueAsString(map);


        //when
        mockMvc.perform(
                        patch("/v1/members/password")
                                .header(accessHeader, BEARER + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updatePassword))
                .andExpect(status().is(new SodevApplicationException(ErrorCode.BAD_REQUEST).getErrorCode().getStatus().value()));

        //then
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
        assertThat(passwordEncoder.matches(password, member.getPassword())).isTrue();
        assertThat(passwordEncoder.matches(password + "!@#!@$%@%%@", member.getPassword())).isFalse();
    }

    @Test
    public void 비밀번호수정_변경_전_비밀번호_검증_에러() throws Exception {
        //given
        String joinData = objectMapper.writeValueAsString(new MemberJoinRequest(email, password, nickName, phone));
        join(joinData);

        String accessToken = getAccessToken();

        Map<String, Object> map = new HashMap<>();
        map.put("checkPassword", password + 1);
        map.put("toBePassword", password + "!@#");

        String updatePassword = objectMapper.writeValueAsString(map);

        //when
        mockMvc.perform(
                        patch("/v1/members/password")
                                .header(accessHeader, BEARER + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updatePassword))
                .andExpect(status().is(new SodevApplicationException(ErrorCode.INVALID_PASSWORD).getErrorCode().getStatus().value()));

        //then
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
        assertThat(passwordEncoder.matches(password, member.getPassword())).isTrue();
        assertThat(passwordEncoder.matches(password + "!@#", member.getPassword())).isFalse();
    }

    @Test
    public void 회원탈퇴_성공() throws Exception {
        //given
        String joinData = objectMapper.writeValueAsString(new MemberJoinRequest(email, password, nickName, phone));
        join(joinData);

        String accessToken = getAccessToken();

        Map<String, Object> map = new HashMap<>();
        map.put("checkPassword", password);

        String updatePassword = objectMapper.writeValueAsString(map);

        //when
        mockMvc.perform(
                        delete("/v1/members")
                                .header(accessHeader, BEARER + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updatePassword))
                .andExpect(status().isOk());

        //then
        assertThrows(SodevApplicationException.class,
                () -> memberRepository.findByEmail(email).orElseThrow(
                        () -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND)));
    }

    @Test
    public void 회원탈퇴_비밀번호_검증_실패() throws Exception {
        //given
        String joinData = objectMapper.writeValueAsString(new MemberJoinRequest(email, password, nickName, phone));
        join(joinData);

        String accessToken = getAccessToken();

        Map<String, Object> map = new HashMap<>();
        map.put("checkPassword", password + 1);

        String updatePassword = objectMapper.writeValueAsString(map);

        //when
        mockMvc.perform(
                        delete("/v1/members")
                                .header(accessHeader, BEARER + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updatePassword))
                .andExpect(status().is(new SodevApplicationException(ErrorCode.INVALID_PASSWORD).getErrorCode().getStatus().value()));

        //then
        assertDoesNotThrow(() -> memberRepository.findByEmail(email));
    }

    @Test
    public void 회원탈퇴_엑세스토큰_틀려서_실패() throws Exception {
        //given
        String joinData = objectMapper.writeValueAsString(new MemberJoinRequest(email, password, nickName, phone));
        join(joinData);

        String accessToken = getAccessToken();

        Map<String, Object> map = new HashMap<>();
        map.put("checkPassword", password);

        String updatePassword = objectMapper.writeValueAsString(map);

        //when
        mockMvc.perform(
                        delete("/v1/members")
                                .header(accessHeader, BEARER + accessToken + 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updatePassword))
                .andExpect(status().is(new SodevApplicationException(ErrorCode.INVALID_TOKEN).getErrorCode().getStatus().value()));

        //then
        assertDoesNotThrow(() -> memberRepository.findByEmail(email));
    }

    @Test
    public void 회원가입시_닉네임_중복_실패() throws Exception {
        //given
        String joinData = objectMapper.writeValueAsString(new MemberJoinRequest(email, password, nickName, phone));
        String joinData2 = objectMapper.writeValueAsString(new MemberJoinRequest("test" + email, password, nickName, phone));
        join(joinData);

        //when & then
        mockMvc.perform(
                        post(SIGN_UP_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(joinData2))
                .andExpect(status().is(new SodevApplicationException(ErrorCode.DUPLICATE_USER_NICKNAME).getErrorCode().getStatus().value()));

    }

    @Test
    public void 회원정보_수정시_닉네임_중복_실패() throws Exception {
        //given
        String joinData = objectMapper.writeValueAsString(new MemberJoinRequest(email, password, nickName, phone));
        String joinData2 = objectMapper.writeValueAsString(new MemberJoinRequest("join2" + email, password, nickName + 2, phone));
        join(joinData);
        join(joinData2);


        String accessToken = getAccessToken();

        String updateData = objectMapper.writeValueAsString(new MemberUpdateRequest("join2" + email, nickName, phone, null, null));
        //when & then
        mockMvc.perform(
                        patch("/v1/members")
                                .header(accessHeader, BEARER + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateData))
                .andExpect(status().is(new SodevApplicationException(ErrorCode.DUPLICATE_USER_NICKNAME).getErrorCode().getStatus().value()));

    }

}

package dev.sodev.domain.follow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sodev.domain.follow.Follow;
import dev.sodev.domain.follow.repository.FollowCustomRepository;
import dev.sodev.domain.follow.repository.FollowRepository;
import dev.sodev.domain.follow.service.FollowService;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.member.dto.request.MemberJoinRequest;
import dev.sodev.domain.member.dto.request.MemberLoginRequest;
import dev.sodev.domain.member.repository.MemberRepository;
import dev.sodev.domain.member.service.MemberService;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class FollowControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired ObjectMapper objectMapper;
    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired FollowService followService;
    @Autowired FollowRepository followRepository;
    @Autowired FollowCustomRepository followCustomRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private static String SIGN_UP_URL = "/v1/join";

    private String email = "sodev@sodev.com";
    private String password = "asdf1234!";
    private String nickName = "testNick";
    private String phone = "010-1234-1234";

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

    private void join(String joinData) throws Exception {
        mockMvc.perform(
                        post(SIGN_UP_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(joinData))
                .andExpect(status().isOk());
    }

    @AfterEach
    public void clear() {
        em.flush();
        em.clear();
    }

    @Test
    public void 팔로우_성공() throws Exception {
        // given
        String joinData = objectMapper.writeValueAsString(new MemberJoinRequest(email, password, nickName, phone));

        String joinData2 = objectMapper.writeValueAsString(new MemberJoinRequest("test"+email, password, "test"+nickName, phone));

        join(joinData);
        join(joinData2);

        Member member1 = memberRepository.findByEmail(email).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
        Member member2 = memberRepository.findByEmail("test"+email).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));

        String accessToken = getAccessToken();

        // when 1번 회원이 2번 회원 팔로우
        mockMvc.perform(
                post("/v1/members/"+member2.getId()+"/follow")
                        .header(accessHeader, BEARER + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                member2.getId()
                        ))).andExpect(status().isOk());

        Follow follow = followRepository.findAll().get(0);

        log.info("follow={}", follow.getId());
        log.info("follow={}", follow.getFromMember().getEmail());
        log.info("follow={}", follow.getToMember().getEmail());

        // then
        assertThat(memberRepository.findById(member1.getId()).orElseThrow().getEmail()).isEqualTo("sodev@sodev.com");
        assertThat(memberRepository.findById(member2.getId()).orElseThrow().getEmail()).isEqualTo("testsodev@sodev.com");
        assertThat(followRepository.findById(follow.getId()).orElseThrow().getFromMember().getEmail()).isEqualTo("sodev@sodev.com");
        assertThat(followRepository.findById(follow.getId()).orElseThrow().getToMember().getEmail()).isEqualTo("testsodev@sodev.com");
        assertThat(followRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    public void 언팔로우_성공() throws Exception {
        // given
        String joinData = objectMapper.writeValueAsString(new MemberJoinRequest(email, password, nickName, phone));

        String joinData2 = objectMapper.writeValueAsString(new MemberJoinRequest("test"+email, password, "test"+nickName, phone));

        join(joinData);
        join(joinData2);

        Member member1 = memberRepository.findByEmail(email).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
        Member member2 = memberRepository.findByEmail("test"+email).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));

        String accessToken = getAccessToken();

        // 1번 회원이 2번 회원 팔로우
        mockMvc.perform(
                post("/v1/members/"+member2.getId()+"/follow")
                        .header(accessHeader, BEARER + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                member2.getId()
                        ))).andExpect(status().isOk());

        Follow follow = followRepository.findAll().get(0);
        log.info("follow={}", follow.getId());
        log.info("follow={}", follow.getFromMember().getEmail());
        log.info("follow={}", follow.getToMember().getEmail());

        assertThat(memberRepository.findById(member1.getId()).orElseThrow().getEmail()).isEqualTo("sodev@sodev.com");
        assertThat(memberRepository.findById(member2.getId()).orElseThrow().getEmail()).isEqualTo("testsodev@sodev.com");
        assertThat(followRepository.findById(follow.getId()).orElseThrow().getFromMember().getEmail()).isEqualTo("sodev@sodev.com");
        assertThat(followRepository.findById(follow.getId()).orElseThrow().getToMember().getEmail()).isEqualTo("testsodev@sodev.com");
        assertThat(followRepository.findAll().size()).isEqualTo(1);

        // when 1번 회원이 2번 회원 팔로우 취소
        mockMvc.perform(
                delete("/v1/members/"+member2.getId()+"/follow")
                        .header(accessHeader, BEARER + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                member2.getId()
                        ))).andExpect(status().isOk());

        assertThat(followRepository.findAll().size()).isEqualTo(0);
    }

    @Test
    public void 본인이_본인을_팔로우_요청_실패() throws Exception {
        // given
        String joinData = objectMapper.writeValueAsString(new MemberJoinRequest(email, password, nickName, phone));

        join(joinData);

        Member member1 = memberRepository.findByEmail(email).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));

        String accessToken = getAccessToken();

        // when 1번 회원이 2번 회원 팔로우
        mockMvc.perform(
                post("/v1/members/"+member1.getId()+"/follow")
                        .header(accessHeader, BEARER + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                member1.getId()
                        ))).andExpect(status().is(new SodevApplicationException(ErrorCode.BAD_REQUEST).getErrorCode().getStatus().value()));

        // then
        assertThat(followRepository.findAll().size()).isEqualTo(0);
        assertThat(member1.getFollower()).isEqualTo(0);
        assertThat(member1.getFollowing()).isEqualTo(0);
    }

    @Test
    public void 본인이_본인을_언팔로우_요청_실패() throws Exception {
        // given
        String joinData = objectMapper.writeValueAsString(new MemberJoinRequest(email, password, nickName, phone));

        join(joinData);

        Member member1 = memberRepository.findByEmail(email).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));

        String accessToken = getAccessToken();

        // when 1번 회원이 2번 회원 팔로우
        mockMvc.perform(
                delete("/v1/members/"+member1.getId()+"/follow")
                        .header(accessHeader, BEARER + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                member1.getId()
                        ))).andExpect(status().is(new SodevApplicationException(ErrorCode.BAD_REQUEST).getErrorCode().getStatus().value()));

        // then
        assertThat(followRepository.findAll().size()).isEqualTo(0);
        assertThat(member1.getFollower()).isEqualTo(0);
        assertThat(member1.getFollowing()).isEqualTo(0);
    }

}

package dev.sodev.domain.follow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sodev.domain.follow.Follow;
import dev.sodev.domain.follow.dto.FollowRequest;
import dev.sodev.domain.follow.repository.FollowCustomRepository;
import dev.sodev.domain.follow.repository.FollowRepository;
import dev.sodev.domain.follow.service.FollowService;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.member.dto.MemberWithdrawal;
import dev.sodev.domain.member.dto.request.MemberJoinRequest;
import dev.sodev.domain.member.dto.request.MemberLoginRequest;
import dev.sodev.domain.member.repository.MemberRepository;
import dev.sodev.domain.member.service.MemberService;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import dev.sodev.global.security.dto.JsonWebTokenDto;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    private static String LOGIN_URL = "/v1/login";

    private String email = "sodev@sodev.com";
    private String password = "asdf1234!";
    private String nickName = "testNick";
    private String phone = "010-1234-1234";


    private JsonWebTokenDto getAccessToken() throws Exception {
        MemberLoginRequest request = MemberLoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        MvcResult result = mockMvc.perform(
                        post(LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()).andReturn();

        String accessToken = result.getResponse().getHeader("Authorization");
        String refreshToken = result.getResponse().getCookie("refresh-token").toString();
        return new JsonWebTokenDto("ROLE_MEMBER", accessToken, refreshToken);
    }

    private void join(String joinData) throws Exception {
        mockMvc.perform(
                        post(SIGN_UP_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(joinData))
                .andExpect(status().isOk());
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


        JsonWebTokenDto tokenDto = getAccessToken();

        // when 1번 회원이 2번 회원 팔로우
        mockMvc.perform(
                post("/v1/members/"+member2.getId()+"/follow")
                        .header("Authorization", tokenDto.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new FollowRequest(member2.getId())
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

        JsonWebTokenDto tokenDto = getAccessToken();

        // 1번 회원이 2번 회원 팔로우
        mockMvc.perform(
                post("/v1/members/"+member2.getId()+"/follow")
                        .header("Authorization", tokenDto.accessToken())
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
                        .header("Authorization", tokenDto.accessToken())
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

        JsonWebTokenDto tokenDto = getAccessToken();

        // when 1번 회원이 2번 회원 팔로우
        mockMvc.perform(
                post("/v1/members/"+member1.getId()+"/follow")
                        .header("Authorization", tokenDto.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                member1.getId()
                        ))).andExpect(status().is(new SodevApplicationException(ErrorCode.BAD_REQUEST).getErrorCode().getStatus().value()));

        // then
        assertThat(followRepository.findAll().size()).isEqualTo(0);
        assertThat(member1.getFollowers().size()).isEqualTo(0);
        assertThat(member1.getFollowing().size()).isEqualTo(0);
    }

    @Test
    public void 본인이_본인을_언팔로우_요청_실패() throws Exception {
        // given
        String joinData = objectMapper.writeValueAsString(new MemberJoinRequest(email, password, nickName, phone));

        join(joinData);

        Member member1 = memberRepository.findByEmail(email).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));

        JsonWebTokenDto tokenDto = getAccessToken();

        // when 1번 회원이 2번 회원 팔로우
        mockMvc.perform(
                delete("/v1/members/"+member1.getId()+"/follow")
                        .header("Authorization", tokenDto.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                member1.getId()
                        ))).andExpect(status().is(new SodevApplicationException(ErrorCode.BAD_REQUEST).getErrorCode().getStatus().value()));

        // then
        assertThat(followRepository.findAll().size()).isEqualTo(0);
        assertThat(member1.getFollowers().size()).isEqualTo(0);
        assertThat(member1.getFollowing().size()).isEqualTo(0);
    }

    @Test
    public void 팔로잉_팔로우_관계인_회원이_탈퇴할시_팔로우도_삭제() throws Exception {
        // given
        String joinData = objectMapper.writeValueAsString(new MemberJoinRequest(email, password, nickName, phone));

        String joinData2 = objectMapper.writeValueAsString(new MemberJoinRequest("test"+email, password, "test"+nickName, phone));

        join(joinData);
        join(joinData2);

        Member member1 = memberRepository.findByEmail(email).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
        Member member2 = memberRepository.findByEmail("test"+email).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));

        JsonWebTokenDto tokenDto = getAccessToken();

        // 1번 회원이 2번 회원 팔로우
        mockMvc.perform(
                post("/v1/members/"+member2.getId()+"/follow")
                        .header("Authorization", tokenDto.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new FollowRequest(member2.getId())
                        ))).andExpect(status().isOk());

        Follow follow = followRepository.findAll().get(0);

        assertThat(memberRepository.findById(member1.getId()).orElseThrow().getEmail()).isEqualTo("sodev@sodev.com");
        assertThat(memberRepository.findById(member2.getId()).orElseThrow().getEmail()).isEqualTo("testsodev@sodev.com");
        assertThat(followRepository.findById(follow.getId()).orElseThrow().getFromMember().getEmail()).isEqualTo("sodev@sodev.com");
        assertThat(followRepository.findById(follow.getId()).orElseThrow().getToMember().getEmail()).isEqualTo("testsodev@sodev.com");
        assertThat(followRepository.findAll().size()).isEqualTo(1);

        em.flush();
        em.clear();

        // when 1번회원이 2번 회원을 팔로잉 하는 상황에
        // 1번 회원이 탈퇴하면 2번 회원의 팔로우가 없어야 한다.
        mockMvc.perform(
                delete("/v1/members")
                        .header("Authorization", tokenDto.accessToken())
                        .cookie(new Cookie("refresh-token", tokenDto.refreshToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new MemberWithdrawal(password)
                        ))).andExpect(status().isOk());

        em.flush();
        em.clear();

        // 1번 회원이 탈퇴해서 회원 데이터에 없고(removed_at != null 이라서 Member 엔티티의 @Where 에 걸림)
        assertThat(memberRepository.existsByEmail(member1.getEmail())).isFalse();
        // 2번 회원의 팔로워였던 1번 회원이 없고 팔로워가 0명이다.
        assertThat(memberRepository.findById(member2.getId()).orElseThrow().getFollowers().size()).isEqualTo(0);
    }

}

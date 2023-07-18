//package dev.sodev.domain.member.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import dev.sodev.domain.member.Member;
//import dev.sodev.domain.enums.Auth;
//import dev.sodev.domain.member.dto.request.MemberLoginRequest;
//import dev.sodev.domain.member.repository.MemberRepository;
//import dev.sodev.global.exception.ErrorCode;
//import dev.sodev.global.exception.SodevApplicationException;
//import dev.sodev.global.jwt.AuthService;
//import dev.sodev.global.redis.RedisRepositoryConfig;
//import dev.sodev.global.redis.RedisService;
//import jakarta.persistence.EntityManager;
//import lombok.extern.slf4j.Slf4j;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//import org.springframework.transaction.annotation.Transactional;
//
//
//import static org.springframework.http.HttpHeaders.SET_COOKIE;
//import static org.springframework.http.MediaType.APPLICATION_JSON;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@Slf4j
//@Transactional
//@SpringBootTest
//@AutoConfigureMockMvc
//public class LoginTest {
//
//    @Autowired MockMvc mockMvc;
//    @Autowired MemberRepository memberRepository;
//    @Autowired EntityManager em;
//    @Autowired BCryptPasswordEncoder passwordEncoder;
//
//    ObjectMapper objectMapper = new ObjectMapper();
//
//    private static String LOGIN_URL = "/v1/login";
//    private static String EMAIL = "member@email.com";
//    private static String PASSWORD = "asdf1234!";
//
//    private void clear() {
//        em.flush();
//        em.clear();
//    }
//
//    @BeforeEach
//    private void init() {
//        memberRepository.save(Member.builder()
//                .email(EMAIL)
//                .password(passwordEncoder.encode(PASSWORD))
//                .nickName("NickName1")
//                .auth(Auth.MEMBER)
//                .build());
//        clear();
//    }
//
//    @Test
//    public void 로그인_성공() throws Exception {
//        // given
//        MemberLoginRequest request = MemberLoginRequest.builder()
//                .email(EMAIL)
//                .password(PASSWORD).build();
//
//        // when
//        MvcResult result = mockMvc.perform(
//                        post(LOGIN_URL)
//                                .contentType(APPLICATION_JSON)
//                                .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk()).andReturn();
//
//        // then
//        String accessToken = result.getResponse().getHeader("Authorization");
//        String refreshToken = result.getResponse().getHeader(SET_COOKIE);
//        Assertions.assertThat(accessToken).startsWith("Bearer ");
//        Assertions.assertThat(refreshToken).startsWith("refresh-token=");
//    }
//
//    @Test
//    public void 로그인_아이디_오류_실패() throws Exception {
//        // given
//        MemberLoginRequest request = MemberLoginRequest.builder()
//                .email(EMAIL +1)
//                .password(PASSWORD).build();
//
//        // when
//        MvcResult result = mockMvc.perform(
//                        post("/v1/login")
//                                .contentType(APPLICATION_JSON)
//                                .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().is(new SodevApplicationException(ErrorCode.EMAIL_NOT_FOUND).getErrorCode().getStatus().value())).andReturn(); // TODO: security onAuthenticationFailure 커스텀 에러로 잡아야함..
//
//        // then
//        String accessToken = result.getResponse().getHeader("Authorization");
//        String refreshToken = result.getResponse().getHeader(SET_COOKIE);
//        Assertions.assertThat(accessToken).isNull();
//        Assertions.assertThat(refreshToken).isNull();
//    }
//
//    @Test
//    public void 로그인_비밀번호_오류_실패() throws Exception {
//        // given
//        MemberLoginRequest request = MemberLoginRequest.builder()
//                .email(EMAIL)
//                .password(PASSWORD+1).build();
//
//        // when
//        MvcResult result = mockMvc.perform(
//                        post("/v1/login")
//                                .contentType(APPLICATION_JSON)
//                                .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().is(new SodevApplicationException(ErrorCode.BAD_CREDENTIAL).getErrorCode().getStatus().value())).andReturn();
//
//        // then
//        String accessToken = result.getResponse().getHeader("Authorization");
//        String refreshToken = result.getResponse().getHeader(SET_COOKIE);
//        Assertions.assertThat(accessToken).isNull();
//        Assertions.assertThat(refreshToken).isNull();
//    }
//
//}
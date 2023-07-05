package dev.sodev.global.login;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.enums.Auth;
import dev.sodev.domain.member.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class LoginTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager em;

    PasswordEncoder delegatingPasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    ObjectMapper objectMapper = new ObjectMapper();

    private static String KEY_EMAIL = "email";
    private static String KEY_PASSWORD = "password";
    private static String MEMBER_EMAIL = "member@email.com";
    private static String PASSWORD = "123456789";

    private static String LOGIN_RUL = "/v1/login";


    private void clear() {
        em.flush();
        em.clear();
    }


    @BeforeEach
    private void init() {
        memberRepository.save(Member.builder()
                .email(MEMBER_EMAIL)
                .password(delegatingPasswordEncoder.encode(PASSWORD))
                .nickName("NickName1")
                .auth(Auth.MEMBER)
                .build());
        clear();
    }

    private Map getUsernamePasswordMap(String username, String password) {
        Map<String, String> map = new HashMap<>();
        map.put(KEY_EMAIL, username);
        map.put(KEY_PASSWORD, password);
        return map;
    }


    private ResultActions perform(String url, MediaType mediaType, Map usernamePasswordMap) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders
                .post(url)
                .contentType(mediaType)
                .content(objectMapper.writeValueAsString(usernamePasswordMap)));
    }

    @Test
    public void 로그인_성공() throws Exception {
        //given
        Map<String, String> map = getUsernamePasswordMap(MEMBER_EMAIL, PASSWORD);

        //when, then
        MvcResult result = perform(LOGIN_RUL, APPLICATION_JSON, map)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void 로그인_실패_아이디틀림() throws Exception {
        //given
        Map<String, String> map = getUsernamePasswordMap(MEMBER_EMAIL+"123", PASSWORD);

        //when, then
        MvcResult result = perform(LOGIN_RUL, APPLICATION_JSON, map)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

    }

    @Test
    public void 로그인_실패_비밀번호틀림() throws Exception {
        //given
        Map<String, String> map = getUsernamePasswordMap(MEMBER_EMAIL, PASSWORD+"123");

        //when, then
        MvcResult result = perform(LOGIN_RUL, APPLICATION_JSON, map)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

    }

    @Test
    public void 로그인_주소가_틀리면_FORBIDDEN() throws Exception {
        //given
        Map<String, String> map = getUsernamePasswordMap(MEMBER_EMAIL, PASSWORD);

        //when, then
        perform(LOGIN_RUL+"123", APPLICATION_JSON, map)
                .andDo(print())
                .andExpect(status().isForbidden());

    }

    @Test
    public void 로그인_데이터형식_JSON이_아니면_200() throws Exception {
        //given
        Map<String, String> map = getUsernamePasswordMap(MEMBER_EMAIL, PASSWORD);

        //when, then
        perform(LOGIN_RUL, APPLICATION_FORM_URLENCODED, map)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void 로그인_HTTP_METHOD_GET이면_NOTFOUND() throws Exception {
        //given
        Map<String, String> map = getUsernamePasswordMap(MEMBER_EMAIL, PASSWORD);

        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .get(LOGIN_RUL)
                        .contentType(APPLICATION_FORM_URLENCODED)
                        .content(objectMapper.writeValueAsString(map)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void 오류_로그인_HTTP_METHOD_PUT이면_NOTFOUND() throws Exception {
        //given
        Map<String, String> map = getUsernamePasswordMap(MEMBER_EMAIL, PASSWORD);

        //when
        mockMvc.perform(MockMvcRequestBuilders
                        .put(LOGIN_RUL)
                        .contentType(APPLICATION_FORM_URLENCODED)
                        .content(objectMapper.writeValueAsString(map)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
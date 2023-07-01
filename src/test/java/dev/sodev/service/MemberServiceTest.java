package dev.sodev.service;

import dev.sodev.controller.request.MemberJoinRequest;
import dev.sodev.controller.request.MemberLoginRequest;
import dev.sodev.controller.response.MemberJoinResponse;
import dev.sodev.domain.MemberAuth;
import dev.sodev.exception.ErrorCode;
import dev.sodev.exception.SodevApplicationException;
import dev.sodev.repository.MemberRepository;
import dev.sodev.domain.entity.Member;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class MemberServiceTest {
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    public static void init() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    public static void close() {
        factory.close();
    }

    @Autowired
    MemberService memberService;

    @MockBean
    MemberRepository memberRepository;

    @MockBean
    BCryptPasswordEncoder passwordEncoder;


    @Test
    @DisplayName("회원가입이 정상적으로 가능한 경우")
    void 회원가입이_정상적으로_작동되는경우() {
        MemberJoinRequest memberJoinRequest = MemberJoinRequest.builder()
                .email("a@naver.com")
                .pwd("1234!Qwerty")
                .phone("010-1111-1111")
                .nickName("TEST")
                .build();
        // fixture member

        Member member = Member.builder()
                .email("a@naver.com")
                .pwd("1234!Qwerty")
                .phone("010-1111-1111")
                .nickName("TEST")
                .build();

        when(memberRepository.findByEmail(memberJoinRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(member.getPwd())).thenReturn("passwordEncode");
        when(memberRepository.save(any())).thenReturn(member);

        Assertions.assertDoesNotThrow( () -> memberService.join(memberJoinRequest));

    }
    @Test
    @DisplayName("아이디가 중복일경우 에러반환")
    void 아이디_중복일_경우_에러반환() {

        MemberJoinRequest memberJoinRequest = MemberJoinRequest.builder()
                .email("a@naver.com")
                .pwd("1234!Qwerty")
                .phone("010-1111-1111")
                .nickName("TEST")
                .build();

        // fixture member

        Member member = Member.builder()
                .email("a@naver.com")
                .pwd("1234!Qwerty")
                .phone("010-1111-1111")
                .nickName("TEST")
                .build();

        //  fixture 반환
        when(memberRepository.findByEmail(memberJoinRequest.getEmail())).thenReturn(Optional.of(member));
        when(memberRepository.save(any())).thenReturn(Optional.of(member));

        SodevApplicationException e = Assertions.assertThrows(SodevApplicationException.class, () -> memberService.join(memberJoinRequest));
        Assertions.assertEquals(ErrorCode.DUPLICATE_USER_ID, e.getErrorCode());

    }
    @Test
    @DisplayName("회원가입 시 Valid 성공일 경우")
    void 유효성_검사_성공일경우() {
        MemberJoinRequest memberJoinRequest = MemberJoinRequest.builder()
                .email("a@naver.com")
                .pwd("1234!Qwerty")
                .phone("010-1111-1111")
                .nickName("TEST")
                .build();
        // when
        Set<ConstraintViolation<MemberJoinRequest>> violations = validator.validate(memberJoinRequest);

        // then
        Assertions.assertEquals(0, violations.size());

    }

    @Test
    @DisplayName("아이디를 입력하지 않았을경우 에러반환")
    void 아이디가_공백일경우() {
        // given
        MemberJoinRequest memberJoinRequest = MemberJoinRequest.builder()
                .email("")
                .pwd("1234!Qwerty")
                .phone("010-1111-1111")
                .nickName("TEST")
                .build();
        // when
        Set<ConstraintViolation<MemberJoinRequest>> violations = validator.validate(memberJoinRequest);

        // then
        violations.forEach( error -> {
            Assertions.assertEquals("아이디를 입력해주세요.", error.getMessage());
        });
    }
    @Test
    @DisplayName("ID가 이메일 형식이 아닌경우 에러반환")
    void ID가_이메일형식이_아닌경우_에러반환() {
        // given
        MemberJoinRequest memberJoinRequest = MemberJoinRequest.builder()
                .email("a")
                .pwd("1234!Qwerty")
                .phone("010-1111-1111")
                .nickName("TEST")
                .build();
        // when
        Set<ConstraintViolation<MemberJoinRequest>> violations = validator.validate(memberJoinRequest);

        // then
        violations.forEach( error -> {
            Assertions.assertEquals("아이디는 email 형식 이어야 합니다.", error.getMessage());
        });
    }

    @Test
    @DisplayName("비밀번호를 입력하지 않았을 경우 에러반환")
    void 비밀번호가_공백인경우() {
        // given - id가 이메일 형식이 아닌경우
        MemberJoinRequest memberJoinRequest = MemberJoinRequest.builder()
                .email("a@naver.com")
                .pwd(null)
                .phone("010-1111-1111")
                .nickName("TEST")
                .build();
        // when
        Set<ConstraintViolation<MemberJoinRequest>> violations = validator.validate(memberJoinRequest);

        // then
        violations.forEach( error -> {
            Assertions.assertEquals("비밀번호를 입력해주세요.", error.getMessage());
        });
    }
    @Test
    @DisplayName("비밀번호 조건에 안맞을경우 에러반환")
    void 비밀번호_조건에_안맞을경우_에러반환() {
        // given - id가 이메일 형식이 아닌경우
        MemberJoinRequest memberJoinRequest = MemberJoinRequest.builder()
                .email("a@naver.com")
                .pwd("123")
                .phone("010-1111-1111")
                .nickName("TEST")
                .build();
        // when
        Set<ConstraintViolation<MemberJoinRequest>> violations = validator.validate(memberJoinRequest);

        // then
        violations.forEach( error -> {
            Assertions.assertEquals("비밀번호는 영문자와 숫자, 특수기호가 적어도 1개 이상 포함된 6자~12자의 비밀번호여야 합니다.", error.getMessage());
        });
    }

    @Test
    @DisplayName("핸드폰번호 형식에 안맞을경우 에러반환")
    void 핸드폰번호_형식에_안맞을경우_에러반환() {
        // given - id가 이메일 형식이 아닌경우
        MemberJoinRequest memberJoinRequest = MemberJoinRequest.builder()
                .email("a@naver.com")
                .pwd("1234!Qwerty")
                .phone("010")
                .nickName("TEST")
                .build();
        // when
        Set<ConstraintViolation<MemberJoinRequest>> violations = validator.validate(memberJoinRequest);

        // then
        violations.forEach( error -> {
            Assertions.assertEquals("양식에 맞게 입력해주세요. ex)010-1234-5678", error.getMessage());
        });
    }



    @Test
    @DisplayName("닉네임을 입력하지 않았을 경우")
    void 닉네임이_공백일경우() {
        // given - id가 이메일 형식이 아닌경우
        MemberJoinRequest memberJoinRequest = MemberJoinRequest.builder()
                .email("a@naver.com")
                .pwd("1234!Qwerty")
                .phone("010-1111-1111")
                .nickName("")
                .build();
        // when
        Set<ConstraintViolation<MemberJoinRequest>> violations = validator.validate(memberJoinRequest);

        // then
        violations.forEach( error -> {
            Assertions.assertEquals("닉네임을 입력해주세요.", error.getMessage());
        });
    }

    @Test
    public void 로그인이_성공하는_경우() throws Exception {
        MemberLoginRequest request = MemberLoginRequest.builder()
                .email("sodev@sodev.com")
                .pwd("asdf1234!")
                .build();
        Member member = Member.builder()
                .email(request.getEmail())
                .pwd(passwordEncoder.encode(request.getPwd()))
                .build();

        when(memberRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(request.getPwd(), member.getPwd())).thenReturn(true);
        Assertions.assertDoesNotThrow(() -> memberService.login(request));
    }

    @Test
    public void 로그인시_회원이_존재하지_않는_경우() throws Exception {
        // given
        MemberLoginRequest request = MemberLoginRequest.builder()
                .email("sodev@sodev.com")
                .pwd("asdf1234!")
                .build();

        when(memberRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        SodevApplicationException exception = Assertions.assertThrows(SodevApplicationException.class
                , () -> memberService.login(request));

        Assertions.assertEquals(ErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());

    }

    @Test
    public void 로그인시_비밀번호가_일치하지_않는_경우() throws Exception {
        // given
        MemberLoginRequest request = MemberLoginRequest.builder()
                .email("sodev@sodev.com")
                .pwd("asdf1234!")
                .build();
        Member member = Member.builder()
                .email(request.getEmail())
                .pwd(passwordEncoder.encode("1234asdf!"))
                .build();

        when(memberRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(member));
        SodevApplicationException exception = Assertions.assertThrows(SodevApplicationException.class
                , () -> memberService.login(request));

        Assertions.assertEquals(ErrorCode.INVALID_PASSWORD, exception.getErrorCode());

    }

}
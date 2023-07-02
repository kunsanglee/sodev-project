package dev.sodev.service;

import dev.sodev.controller.request.MemberJoinRequest;
import dev.sodev.controller.request.MemberLoginRequest;
import dev.sodev.controller.response.MemberJoinResponse;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import dev.sodev.repository.MemberRepository;
import dev.sodev.domain.MemberAuth;
import dev.sodev.domain.entity.Member;
import dev.sodev.domain.enums.Auth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;


    public MemberAuth loadUserByUsername(String email) throws UsernameNotFoundException {
        // 회원의 이메일로 엔티티를 조회하고, MemberAuth Dto 로 매핑하여 반환한다.
        return memberRepository.findByEmail(email).map(MemberAuth::fromEntity).orElseThrow(
                        () -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND, String.format("email is %s", email)));
    }

    @Transactional
    public MemberJoinResponse join(MemberJoinRequest request) {
        // 아이디 중복일 시 에러 반환
        memberRepository.findByEmail(request.getEmail()).ifPresent(it -> {
            throw new SodevApplicationException(ErrorCode.DUPLICATE_USER_ID, String.format("%s is duplicated", request.getEmail()));
        });

        // 중복이 아닐경우 이메일형식의 아이디와 비밀번호 암호화 권한은 MEMBER 으로 회원가입
        Member joinMember = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickName(request.getNickName())
                .phone(request.getPhone())
                .auth(Auth.MEMBER)
                .build();

        memberRepository.save(joinMember);

        return new MemberJoinResponse("회원가입이 완료되었습니다.");
    }

}

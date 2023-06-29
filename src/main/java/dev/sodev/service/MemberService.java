package dev.sodev.service;

import dev.sodev.controller.request.MemberJoinRequest;
import dev.sodev.controller.response.MemberJoinResponse;
import dev.sodev.exception.ErrorCode;
import dev.sodev.exception.SodevApplicationException;
import dev.sodev.repository.MemberRepository;
import dev.sodev.repository.entity.Member;
import dev.sodev.repository.enums.Auth;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public MemberJoinResponse join(MemberJoinRequest request) {
        // 아이디 중복일 시 에러 반환
        memberRepository.findMemberByEmail(request.getEmail()).ifPresent(it -> {
            throw new SodevApplicationException(ErrorCode.DUPLICATE_USER_ID, String.format("%s is duplicated", request.getEmail()));
        });

        // 중복이 아닐경우 이메일형식의 아이디와 비밀번호 암호화 권한은 MEMBER 으로 회원가입
        Member joinMember = Member.builder()
                .email(request.getEmail())
                .pwd(passwordEncoder.encode(request.getPwd()))
                .nickName(request.getNickName())
                .phone(request.getPhone())
                .auth(Auth.MEMBER)
                .build();

        memberRepository.save(joinMember);

        return new MemberJoinResponse("회원가입이 완료되었습니다.");
    }

}

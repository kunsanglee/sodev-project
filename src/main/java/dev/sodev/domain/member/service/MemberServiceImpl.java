package dev.sodev.domain.member.service;

import dev.sodev.domain.member.dto.request.MemberUpdateRequest;
import dev.sodev.domain.member.dto.request.MemberJoinRequest;
import dev.sodev.domain.member.dto.response.MemberUpdateResponse;
import dev.sodev.domain.member.dto.response.MemberJoinResponse;
import dev.sodev.domain.member.dto.MemberInfo;
import dev.sodev.domain.member.dto.UpdatePassword;
import dev.sodev.global.email.EmailService;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import dev.sodev.domain.member.repository.MemberRepository;
import dev.sodev.domain.member.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static dev.sodev.global.security.utils.SecurityUtil.getMemberEmail;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final EmailService emailService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public MemberJoinResponse join(MemberJoinRequest request) {
        // 아이디 중복, 닉네임 중복일시 에러 반환
        if (isDuplicatedEmail(request.email())) {
            throw new SodevApplicationException(ErrorCode.DUPLICATE_USER_EMAIL);
        }

        if (isDuplicatedNickName(request.nickName())) {
            throw new SodevApplicationException(ErrorCode.DUPLICATE_USER_NICKNAME);
        }

        // 이메일 인증메일 발송 시작 ->
        // 프론트에서 비동기로 이메일 보내고 확인 코드 입력하면 회원가입 신청할 수 있게 변경해야됨.
        // 일단 메일 가는지 확인 OK 주석처리
        /*String emailSignCode = String.valueOf(UUID.randomUUID()).substring(0, 8);
        EmailRequest emailRequest = new EmailRequest(
                request.email(),
                request.nickName() + "님의 SODEV 회원가입 인증메일 입니다.",
                emailSignCode
        );

        try {
            emailService.sendEmail(emailRequest);
        } catch (Exception e) {
            throw new SodevApplicationException(ErrorCode.INTERNAL_SERVER_ERROR, "이메일 인증 도중 에러가 발생했습니다.");
        }*/
        // 이메일 인증메일 발송 끝


        // 중복이 아닐경우 이메일형식의 아이디와 비밀번호 암호화 권한은 Default(MEMBER)로 회원가입
        Member joinMember = Member.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickName(request.nickName())
                .phone(request.phone())
                .build();

        memberRepository.save(joinMember);

        return new MemberJoinResponse("회원가입이 완료되었습니다.");
    }

    @Override
    public MemberInfo getMyInfo() {
        Member member = getMemberBySecurity();
        return MemberInfo.from(member);
    }

    @Override
    public MemberInfo getMemberInfo(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
        return MemberInfo.from(member);
    }

    @Override
    @Transactional
    public MemberUpdateResponse update(MemberUpdateRequest request) {
        if (isDuplicatedNickName(request.nickName())) {
            throw new SodevApplicationException(ErrorCode.DUPLICATE_USER_NICKNAME);
        }

        Member member = getMemberBySecurity();
        member.updateMemberInfo(request);

        return new MemberUpdateResponse("회원정보 수정이 완료됐습니다.");
    }

    @Override
    @Transactional
    public MemberUpdateResponse updatePassword(UpdatePassword updatePassword) {
        Member member = getMemberBySecurity();
        String checkPassword = updatePassword.checkPassword();
        String toBePassword = updatePassword.toBePassword();

        if (!member.matchPassword(passwordEncoder, checkPassword)) {
            throw new SodevApplicationException(ErrorCode.INVALID_PASSWORD);
        }

        member.updatePassword(passwordEncoder, toBePassword);

        return new MemberUpdateResponse("비밀번호 변경이 완료됐습니다.");
    }

    @Override
    public boolean isDuplicatedEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    @Override
    public boolean isDuplicatedNickName(String nickName) {
        return memberRepository.existsByNickName(nickName);
    }

    private Member getMemberBySecurity() {
        return memberRepository.findByEmail(getMemberEmail()).orElseThrow(() ->
                new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
    }
}

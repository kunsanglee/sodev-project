package dev.sodev.domain.member.service;

import dev.sodev.domain.member.dto.request.MemberUpdateRequest;
import dev.sodev.domain.member.dto.request.MemberJoinRequest;
import dev.sodev.domain.member.dto.response.MemberUpdateResponse;
import dev.sodev.domain.member.dto.response.MemberJoinResponse;
import dev.sodev.domain.member.dto.MemberInfo;
import dev.sodev.domain.member.dto.MemberWithdrawal;
import dev.sodev.domain.member.dto.UpdatePassword;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import dev.sodev.domain.member.repository.MemberRepository;
import dev.sodev.domain.member.Member;
import dev.sodev.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public MemberJoinResponse join(MemberJoinRequest request) {
        // 아이디 중복, 닉네임 중복일시 에러 반환
        if (isDuplicatedEmail(request.email())) {
            throw new SodevApplicationException(ErrorCode.DUPLICATE_USER_ID);
        }

        if (isDuplicatedNickName(request.nickName())) {
            throw new SodevApplicationException(ErrorCode.DUPLICATE_USER_NICKNAME);
        }

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

    @Transactional(readOnly = true)
    @Override
    public MemberInfo getMyInfo() {
        Member member = getMemberBySecurity();
        return MemberInfo.from(member);
    }

    @Transactional(readOnly = true)
    @Override
    public MemberInfo getMemberInfo(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
        return MemberInfo.from(member);
    }

    @Override
    public MemberUpdateResponse update(MemberUpdateRequest request) {

        if (isDuplicatedNickName(request.nickName())) {
            throw new SodevApplicationException(ErrorCode.DUPLICATE_USER_NICKNAME);
        }

        Member member = getMemberBySecurity();

        member.updateNickName(request.nickName());
        member.updatePhone(request.phone());
        member.updateIntroduce(request.introduce());
        member.updateImage(request.memberImage());

        return new MemberUpdateResponse("회원정보 수정이 완료됐습니다.");
    }

    @Override
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
    public MemberUpdateResponse withdrawal(MemberWithdrawal memberWithdrawal) {
        Member member = getMemberBySecurity();
        String checkPassword = memberWithdrawal.checkPassword();
        if (!member.matchPassword(passwordEncoder, checkPassword)) {
            throw new SodevApplicationException(ErrorCode.INVALID_PASSWORD);
        }

        member.destroyRefreshToken();
        memberRepository.delete(member);

        return new MemberUpdateResponse("회원 탈퇴가 완료됐습니다.");
    }

    private Member getMemberBySecurity() {
        return memberRepository.findByEmail(SecurityUtil.getMemberEmail()).orElseThrow(() ->
                new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    @Override
    public boolean isDuplicatedEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean isDuplicatedNickName(String nickName) {
        return memberRepository.existsByNickName(nickName);
    }
}

package dev.sodev.domain.member.service;

import dev.sodev.domain.member.dto.request.MemberUpdateRequest;
import dev.sodev.domain.member.dto.request.MemberJoinRequest;
import dev.sodev.domain.member.dto.response.MemberUpdateResponse;
import dev.sodev.domain.member.dto.response.MemberJoinResponse;
import dev.sodev.domain.member.dto.MemberInfo;
import dev.sodev.domain.member.dto.MemberWithdrawal;
import dev.sodev.domain.member.dto.UpdatePassword;

public interface MemberService {

    /**
     * 회원가입
     * 정보 수정
     * 회원 탈퇴
     * 회원정보 조회
     * 이메일 중복 확인
     * 닉네임 중복 확인
     */

    MemberJoinResponse join(MemberJoinRequest memberJoinRequest);

    MemberUpdateResponse update(MemberUpdateRequest memberInfoRequest);

    MemberUpdateResponse updatePassword(UpdatePassword updatePassword, String memberEmail);

    MemberUpdateResponse withdrawal(MemberWithdrawal memberWithdrawal, String memberEmail);

    MemberInfo getMyInfo(String memberEmail);

    MemberInfo getMemberInfo(Long id, String memberEmail);

    boolean isDuplicatedEmail(String email);

    boolean isDuplicatedNickName(String nickName);
}

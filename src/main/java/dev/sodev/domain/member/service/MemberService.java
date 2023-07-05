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
     * 정보수정
     * 회원탈퇴
     * 정보조회
     */

    MemberJoinResponse join(MemberJoinRequest memberJoinRequest);

    MemberUpdateResponse update(MemberUpdateRequest memberInfoRequest);

    MemberUpdateResponse updatePassword(UpdatePassword updatePassword);

    MemberUpdateResponse withdrawal(MemberWithdrawal memberWithdrawal);

    MemberInfo getMemberInfo(Long id);

    MemberInfo getMyInfo();
}

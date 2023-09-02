package dev.sodev.domain.member.repository.query;

import dev.sodev.domain.member.dto.MemberAppliedDto;
import dev.sodev.domain.member.dto.MemberHistoryDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface MemberProjectCustomRepository {

    void deleteAllByApplicantId(Long memberId);

    Slice<MemberAppliedDto> findAppliedProjectsByMemberId(Long memberId, Pageable pageable);

    Slice<MemberHistoryDto> findHistoryProjectsByMemberId(Long memberId, Pageable pageable);
}

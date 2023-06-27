package dev.sodev.service.member;

import dev.sodev.domain.entity.Member;
import dev.sodev.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public Member findMemberById(Long id) {
        return memberRepository.findById(id).orElseThrow();
    }

}

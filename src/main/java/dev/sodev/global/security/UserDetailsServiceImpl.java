package dev.sodev.global.security;

import dev.sodev.domain.member.Member;
import dev.sodev.domain.member.repository.MemberRepository;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    public final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member findMember = memberRepository.findByEmail(email)
                .orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND, "존재하지 않는 회원입니다 -> " + email));

        if(findMember != null){
            return new UserDetailsImpl(findMember);
        }

        return null;
    }
}

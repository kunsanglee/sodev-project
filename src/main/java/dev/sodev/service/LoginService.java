package dev.sodev.service;

import dev.sodev.domain.entity.Member;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import dev.sodev.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String memberEmail) throws UsernameNotFoundException {

        Member member = memberRepository.findByEmail(memberEmail).orElseThrow(() -> new SodevApplicationException(ErrorCode.EMAIL_NOT_FOUND, "존재하지 않는 이메일 입니다."));

        return User.builder().username(member.getEmail())
                .password(member.getPassword())
                .roles(member.getAuth().name())
                .build();
    }

}

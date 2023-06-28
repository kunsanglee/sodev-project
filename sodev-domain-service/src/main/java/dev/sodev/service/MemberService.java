package dev.sodev.service;

import dev.sodev.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberService {

    MemberRepository memberRepository;


}

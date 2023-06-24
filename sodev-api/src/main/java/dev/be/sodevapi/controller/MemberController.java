package dev.be.sodevapi.controller;

import dev.be.sodevcommon.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/members")
    public String member() {
        memberRepository.findAll();
        return "ok!!!";
    }
}

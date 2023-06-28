package dev.sodev.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class MemberController {


    @GetMapping("/members")
    public String member() {
        return "ok!!!";
    }

    @GetMapping("/test")
    public String test() {
        return "deploy success";
    }

    @GetMapping("/nginx")
    public String nginx() {
        return "nginx success";
    }
}

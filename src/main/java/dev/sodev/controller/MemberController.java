package dev.sodev.controller;

import dev.sodev.controller.request.MemberJoinRequest;
import dev.sodev.controller.request.MemberLoginRequest;
import dev.sodev.controller.response.MemberJoinResponse;
import dev.sodev.controller.response.MemberLoginResponse;
import dev.sodev.controller.response.Response;
import dev.sodev.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/v1")
@RestController
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/sign-up")
    public Response<MemberJoinResponse> join(@RequestBody @Valid MemberJoinRequest request) {
        MemberJoinResponse response = memberService.join(request);
        return Response.success(response);
    }

    @PostMapping("/sign-in")
    public Response<MemberLoginResponse> login(@RequestBody @Valid MemberLoginRequest request) {
        return Response.success(new MemberLoginResponse(memberService.login(request)));
    }

    @GetMapping("/nginx")
    public String nginx() {
        return "nginx success";
    }
}

package dev.sodev.controller;

import dev.sodev.controller.request.MemberJoinRequest;
import dev.sodev.controller.response.MemberJoinResponse;
import dev.sodev.controller.response.Response;
import dev.sodev.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping
@RestController
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/sign-up")
    public Response<MemberJoinResponse> member(@RequestBody @Valid MemberJoinRequest request) {
        MemberJoinResponse response = memberService.join(request);
        return Response.success(response);
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

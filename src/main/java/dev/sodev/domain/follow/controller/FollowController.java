package dev.sodev.domain.follow.controller;

import dev.sodev.domain.follow.dto.FollowRequest;
import dev.sodev.domain.follow.dto.FollowResponse;
import dev.sodev.domain.follow.service.FollowService;
import dev.sodev.global.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/v1/members/{id}/follow")
@RestController
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping
    public Response<FollowResponse> follow(@PathVariable Long id) {
        FollowResponse response = followService.follow(new FollowRequest(id));
        return Response.success(response);
    }

    @DeleteMapping
    public Response<FollowResponse> unFollow(@PathVariable Long id) {
        FollowResponse response = followService.unfollow(new FollowRequest(id));
        return Response.success(response);
    }
}

package dev.sodev.domain.follow.controller;

import dev.sodev.domain.follow.dto.FollowDto;
import dev.sodev.domain.follow.dto.FollowRequest;
import dev.sodev.domain.follow.dto.FollowResponse;
import dev.sodev.domain.follow.service.FollowService;
import dev.sodev.global.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/v1/members")
@RestController
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{id}/follow")
    public Response<FollowResponse<Void>> follow(@PathVariable Long id) {
        FollowResponse<Void> response = followService.follow(new FollowRequest(id));
        return Response.success(response);
    }

    @DeleteMapping("/{id}/follow")
    public Response<FollowResponse<Void>> unFollow(@PathVariable Long id) {
        FollowResponse<Void> response = followService.unfollow(new FollowRequest(id));
        return Response.success(response);
    }

    @GetMapping("/follower")
    public Response<FollowResponse<List<FollowDto>>> getFollowers() {
        FollowResponse<List<FollowDto>> response = followService.getFollowers();
        return Response.success(response);
    }

    @GetMapping("/following")
    public Response<FollowResponse<List<FollowDto>>> getFollowing() {
        FollowResponse<List<FollowDto>> response = followService.getFollowing();
        return Response.success(response);
    }

    @GetMapping("{id}/follower")
    public Response<FollowResponse<List<FollowDto>>> getMembersFollowers(@PathVariable Long id) {
        FollowResponse<List<FollowDto>> response = followService.getFollowers();
        return Response.success(response);
    }

    @GetMapping("{id}/following")
    public Response<FollowResponse<List<FollowDto>>> getMembersFollowing(@PathVariable Long id) {
        FollowResponse<List<FollowDto>> response = followService.getFollowing();
        return Response.success(response);
    }
}

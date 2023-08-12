package dev.sodev.domain.follow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import dev.sodev.domain.follow.dto.FollowDto;
import dev.sodev.domain.follow.dto.FollowRequest;
import dev.sodev.domain.follow.dto.FollowResponse;
import dev.sodev.domain.follow.service.FollowService;
import dev.sodev.global.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;




import java.util.List;

@Tag(name = "Follow", description = "팔로우 api")
@RequestMapping("/v1/members")
@RestController
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @Operation(summary = "팔로우 요청", description = "팔로우 하고자 하는 회원의 id로 해당 회원을 팔로우 합니다.")
    @PostMapping("/{id}/follow")
    public Response<FollowResponse<Void>> follow(@PathVariable Long id) {
        FollowResponse<Void> response = followService.follow(new FollowRequest(id));
        return Response.success(response);
    }

    @Operation(summary = "팔로우 삭제", description = "회원의 id로 팔로우 중인 회원을 팔로우 취소합니다.")
    @DeleteMapping("/{id}/follow")
    public Response<FollowResponse<Void>> unFollow(@PathVariable Long id) {
        FollowResponse<Void> response = followService.unfollow(new FollowRequest(id));
        return Response.success(response);
    }

    /**
     * 기존에 있던 본인의 팔로워, 팔로잉 목록도 @PathVariable id 입력해주는게
     * 메서드가 중복되지 않고 깔끔할 것 같아서 제거하고 타 회원 조회할 때 사용하는 api 로 같이 처리.
     */
    @Operation(summary = "팔로워 리스트 요청", description = "회원의 id로 해당 회원을 팔로우 하는 회원 리스트를 요청합니다.")
    @GetMapping("{id}/follower")
    public Response<Slice<FollowDto>> getMembersFollowers(@PathVariable Long id, Pageable pageable) {
        Slice<FollowDto> response = followService.getFollowerByMemberId(id, pageable);
        return Response.success(response);
    }

    @Operation(summary = "팔로잉 리스트 요청", description = "회원의 id로 해당 회원이 팔로우 하는 회원 리스트를 요청합니다.")
    @GetMapping("{id}/following")
    public Response<Slice<FollowDto>> getMembersFollowing(@PathVariable Long id, Pageable pageable) {
        Slice<FollowDto> response = followService.getFollowingByMemberId(id, pageable);
        return Response.success(response);
    }
}

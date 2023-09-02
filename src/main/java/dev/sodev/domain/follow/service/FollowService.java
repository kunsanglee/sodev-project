package dev.sodev.domain.follow.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import dev.sodev.domain.follow.dto.FollowDto;
import dev.sodev.domain.follow.dto.FollowRequest;
import dev.sodev.domain.follow.dto.FollowResponse;


public interface FollowService {

    /**
     * 팔로우하기
     * 팔로우 취소하기
     */
    FollowResponse<Void> follow(FollowRequest request);

    FollowResponse<Void> unfollow(FollowRequest request);

    Slice<FollowDto> getFollowerByMemberId(Long memberId, Pageable pageable);

    Slice<FollowDto> getFollowingByMemberId(Long memberId, Pageable pageable);

}

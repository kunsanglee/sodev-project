package dev.sodev.domain.follow.service;

import dev.sodev.domain.follow.dto.FollowRequest;
import dev.sodev.domain.follow.dto.FollowResponse;

public interface FollowService {

    /**
     * 팔로우하기
     * 팔로우 취소하기
     */
    FollowResponse follow(FollowRequest request);

    FollowResponse unfollow(FollowRequest request);
}

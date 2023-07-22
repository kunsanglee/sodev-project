package dev.sodev.domain.follow.service;

import dev.sodev.domain.follow.dto.FollowDto;
import dev.sodev.domain.follow.dto.FollowRequest;
import dev.sodev.domain.follow.dto.FollowResponse;

import java.util.List;

public interface FollowService {

    /**
     * 팔로우하기
     * 팔로우 취소하기
     */
    FollowResponse<Void> follow(FollowRequest request);

    FollowResponse<Void> unfollow(FollowRequest request);

    FollowResponse<List<FollowDto>> getFollowers();

    FollowResponse<List<FollowDto>> getFollowing();

    FollowResponse<List<FollowDto>> getMembersFollowers(Long id);

    FollowResponse<List<FollowDto>> getMembersFollowing(Long id);
}

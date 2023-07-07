package dev.sodev.domain.follow.service;

import dev.sodev.domain.follow.Follow;
import dev.sodev.domain.follow.dto.FollowRequest;
import dev.sodev.domain.follow.dto.FollowResponse;
import dev.sodev.domain.follow.repository.FollowRepository;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.member.repository.MemberRepository;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import dev.sodev.global.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {
    
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;

    @Override
    public FollowResponse follow(@Valid FollowRequest request) {
        String fromMemberEmail = SecurityUtil.getMemberEmail();
        Member fromMember = memberRepository.findByEmail(fromMemberEmail).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
        if (request.toId().equals(fromMember.getId())) {
            throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "본인을 팔로우할 수 없습니다.");
        }
        Member toMember = memberRepository.findById(request.toId()).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));

        Follow follow = getFollow(fromMember, toMember);

        followRepository.save(follow);
        fromMember.addFollowing();
        toMember.addFollower();

        return new FollowResponse(toMember.getNickName() + "님을 팔로우하기 시작했습니다!");
    }

    @Override
    public FollowResponse unfollow(@Valid FollowRequest request) {
        String fromMemberEmail = SecurityUtil.getMemberEmail();
        Member fromMember = memberRepository.findByEmail(fromMemberEmail).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
        if (request.toId().equals(fromMember.getId())) {
            throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "본인을 언팔로우할 수 없습니다.");
        }
        Member toMember = memberRepository.findById(request.toId()).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));

        followRepository.deleteByFromMemberAndToMember(fromMember, toMember);
        fromMember.subFollowing();
        toMember.subFollower();

        return new FollowResponse(toMember.getNickName() + "님을 언팔로우 하였습니다.");
    }

    private static Follow getFollow(Member fromMember, Member toMember) {
        Follow follow = Follow.builder()
                .fromMember(fromMember)
                .toMember(toMember)
                .build();
        return follow;
    }
}

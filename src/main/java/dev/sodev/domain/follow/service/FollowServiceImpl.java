package dev.sodev.domain.follow.service;

import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Pageable;
import dev.sodev.domain.follow.Follow;
import dev.sodev.domain.follow.dto.FollowDto;
import dev.sodev.domain.follow.dto.FollowRequest;
import dev.sodev.domain.follow.dto.FollowResponse;
import dev.sodev.domain.follow.repository.FollowRepository;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.member.repository.MemberRepository;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import dev.sodev.global.security.utils.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;

    @Override
    public FollowResponse<Void> follow(@Valid FollowRequest request) {
        String fromMemberEmail = SecurityUtil.getMemberEmail();
        Member fromMember = memberRepository.findByEmail(fromMemberEmail).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
        if (request.toId().equals(fromMember.getId())) {
            throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "본인을 팔로우할 수 없습니다.");
        }
        Member toMember = memberRepository.findById(request.toId()).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));

        Follow follow = getFollow(fromMember);
        follow.follow(toMember);

        followRepository.save(follow);

        return new FollowResponse<>(toMember.getNickName() + "님을 팔로우하기 시작했습니다!", null);
    }

    @Override
    public FollowResponse<Void> unfollow(@Valid FollowRequest request) {
        String fromMemberEmail = SecurityUtil.getMemberEmail();
        Member fromMember = memberRepository.findByEmail(fromMemberEmail).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
        if (request.toId().equals(fromMember.getId())) {
            throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "본인을 언팔로우할 수 없습니다.");
        }
        Member toMember = memberRepository.findById(request.toId()).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));

        Follow follow = followRepository.findByFromMemberAndToMember(fromMember, toMember);
        follow.unfollow(toMember);

        followRepository.delete(follow);

        return new FollowResponse<>(toMember.getNickName() + "님을 언팔로우 하였습니다.", null);
    }

    @Override
    public Slice<FollowDto> getFollowerByMemberId(Long memberId, Pageable pageable) {
        Slice<Follow> followings = followRepository.findAllByToMember_Id(memberId, pageable);
        return followings.map(FollowDto::following);
    }

    @Override
    public Slice<FollowDto> getFollowingByMemberId(Long memberId, Pageable pageable) {
        Slice<Follow> followings = followRepository.findAllByFromMember_Id(memberId, pageable);
        return followings.map(FollowDto::following);
    }

    private static Follow getFollow(Member fromMember) {
        return Follow.builder()
                .fromMember(fromMember)
                .build();
    }
}

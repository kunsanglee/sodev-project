package dev.sodev.domain.follow.service;

import dev.sodev.domain.enums.AlarmType;
import dev.sodev.global.kafka.AlarmProducer;
import dev.sodev.global.kafka.event.AlarmEvent;
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
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    public static final String SELF_FOLLOW = "본인을 팔로우할 수 없습니다.";
    public static final String SELF_UNFOLLOW = "본인을 언팔로우할 수 없습니다.";

    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final AlarmProducer alarmProducer;


    @Transactional
    @Override
    public FollowResponse<Void> follow(@Valid FollowRequest request) {
        Member fromMember = getCurrentMember();
        fromMember.validSelfFollow(request, SELF_FOLLOW);
        Member toMember = getTargetMember(request);

        Follow follow = fromMember.follow(toMember);
        followRepository.save(follow);

        sendFollowAlarm(fromMember, toMember);

        return new FollowResponse<>(toMember.getNickName() + "님을 팔로우하기 시작했습니다!", null);
    }

    @Transactional
    @Override
    public FollowResponse<Void> unfollow(@Valid FollowRequest request) {
        Member fromMember = getCurrentMember();
        fromMember.validSelfFollow(request, SELF_UNFOLLOW);
        Member toMember = getTargetMember(request);

        Follow unfollow = fromMember.unfollow(toMember);
        followRepository.delete(unfollow);

        return new FollowResponse<>(toMember.getNickName() + "님을 언팔로우 하였습니다.", null);
    }

    @Override
    public Slice<FollowDto> getFollowerByMemberId(Long memberId, Pageable pageable) {
        Slice<Follow> followers = followRepository.findAllByToMember_Id(memberId, pageable);
        return followers.map(FollowDto::followerFromEntity);
    }

    @Override
    public Slice<FollowDto> getFollowingByMemberId(Long memberId, Pageable pageable) {
        Slice<Follow> followings = followRepository.findAllByFromMember_Id(memberId, pageable);
        return followings.map(FollowDto::followingFromEntity);
    }

    // 현재 요청회원 조회.
    private Member getCurrentMember() {
        String fromMemberEmail = SecurityUtil.getMemberEmail();
        return memberRepository.findByEmail(fromMemberEmail).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
    }

    // 요청 대상회원 조회.
    private Member getTargetMember(FollowRequest request) {
        return memberRepository.findById(request.toId()).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
    }

    // 팔로우 알람 전송.
    private void sendFollowAlarm(Member fromMember, Member toMember) {
        List<Member> receivers = List.of(toMember);
        alarmProducer.send(AlarmEvent.of(AlarmType.NEW_FOLLOWER, fromMember, null, receivers));
    }
}

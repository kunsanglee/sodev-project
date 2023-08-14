package dev.sodev.domain.follow.service;

import dev.sodev.domain.alarm.service.AlarmService;
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
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final AlarmService alarmService;
    private final AlarmProducer alarmProducer;

    @Transactional
    @Override
    public FollowResponse<Void> follow(@Valid FollowRequest request) {
        String fromMemberEmail = SecurityUtil.getMemberEmail();
        Member fromMember = getMember(memberRepository.findByEmail(fromMemberEmail));
        isOtherMember(request, fromMember, "본인을 팔로우할 수 없습니다.");
        Member toMember = getMember(memberRepository.findById(request.toId()));

        Follow follow = getFollow(fromMember);
        follow.follow(toMember);

        log.info("팔로우 저장");
        followRepository.save(follow);

        log.info("팔로우 알림 발송");
        List<Member> receivers = alarmService.alarmsToOne(toMember);
        alarmProducer.send(AlarmEvent.of(AlarmType.NEW_FOLLOWER, fromMember, null, receivers));

        return new FollowResponse<>(toMember.getNickName() + "님을 팔로우하기 시작했습니다!", null);
    }

    @Transactional
    @Override
    public FollowResponse<Void> unfollow(@Valid FollowRequest request) {
        String fromMemberEmail = SecurityUtil.getMemberEmail();
        Member fromMember = getMember(memberRepository.findByEmail(fromMemberEmail));
        isOtherMember(request, fromMember, "본인을 언팔로우할 수 없습니다.");
        Member toMember = getMember(memberRepository.findById(request.toId()));

        Follow follow = followRepository.findByFromMemberAndToMember(fromMember, toMember);
        follow.unfollow(toMember);

        log.info("팔로우 삭제");
        followRepository.delete(follow);

        return new FollowResponse<>(toMember.getNickName() + "님을 언팔로우 하였습니다.", null);
    }

    @Override
    public Slice<FollowDto> getFollowerByMemberId(Long memberId, Pageable pageable) {
        Slice<Follow> followings = followRepository.findAllByToMember_Id(memberId, pageable);
        return followings.map(FollowDto::follower);
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

    // 회원 조회
    private Member getMember(Optional<Member> memberRepository) {
        return memberRepository.orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
    }

    // 본인을 팔로우 하려는지 확인
    private static void isOtherMember(FollowRequest request, Member fromMember, String message) {
        if (request.toId().equals(fromMember.getId())) {
            throw new SodevApplicationException(ErrorCode.BAD_REQUEST, message);
        }
    }
}

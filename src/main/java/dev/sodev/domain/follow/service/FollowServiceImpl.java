package dev.sodev.domain.follow.service;

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
    public FollowResponse<List<FollowDto>> getFollowers() {
        String memberEmail = SecurityUtil.getMemberEmail();
        Member member = memberRepository.findByEmail(memberEmail).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
        List<Follow> followers = followRepository.findAllByToMember(member);
        List<FollowDto> list = followers.stream().map(follow ->
                FollowDto.builder()
                        .memberId(follow.getFromMember().getId())
                        .email(follow.getFromMember().getEmail())
                        .nickName(follow.getFromMember().getNickName())
                        .build()).toList();
        return new FollowResponse<>("팔로워 목록 조회를 완료했습니다.", list);
    }

    @Override
    public FollowResponse<List<FollowDto>> getFollowing() {
        String memberEmail = SecurityUtil.getMemberEmail();
        Member member = memberRepository.findByEmail(memberEmail).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
        List<Follow> following = followRepository.findAllByFromMember(member);
        List<FollowDto> list = following.stream().map(follow ->
                FollowDto.builder()
                        .memberId(follow.getToMember().getId())
                        .email(follow.getToMember().getEmail())
                        .nickName(follow.getToMember().getNickName())
                        .build()).toList();
        return new FollowResponse<>("팔로잉 목록 조회를 완료했습니다.", list);
    }

    @Override
    public FollowResponse<List<FollowDto>> getMembersFollowers(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
        List<Follow> followers = followRepository.findAllByToMember(member);
        List<FollowDto> list = followers.stream().map(follow ->
                FollowDto.builder()
                        .memberId(follow.getFromMember().getId())
                        .email(follow.getFromMember().getEmail())
                        .nickName(follow.getFromMember().getNickName())
                        .build()).toList();
        return new FollowResponse<>("팔로워 목록 조회를 완료했습니다.", list);
    }

    @Override
    public FollowResponse<List<FollowDto>> getMembersFollowing(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
        List<Follow> following = followRepository.findAllByFromMember(member);
        List<FollowDto> list = following.stream().map(follow ->
                FollowDto.builder()
                        .memberId(follow.getToMember().getId())
                        .email(follow.getToMember().getEmail())
                        .nickName(follow.getToMember().getNickName())
                        .build()).toList();
        return new FollowResponse<>("팔로잉 목록 조회를 완료했습니다.", list);
    }

    private static Follow getFollow(Member fromMember) {
        return Follow.builder()
                .fromMember(fromMember)
                .build();
    }
}

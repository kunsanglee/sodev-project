package dev.sodev.domain.member.controller;

import dev.sodev.domain.likes.dto.LikesProjectDto;
import dev.sodev.domain.member.dto.MemberAppliedDto;
import dev.sodev.domain.member.dto.MemberHistoryDto;
import dev.sodev.domain.member.dto.request.MemberJoinRequest;
import dev.sodev.domain.member.dto.request.MemberUpdateRequest;
import dev.sodev.domain.member.dto.response.MemberJoinResponse;
import dev.sodev.domain.member.dto.response.MemberUpdateResponse;
import dev.sodev.domain.project.dto.ProjectDto;
import dev.sodev.domain.project.service.ProjectService;
import dev.sodev.domain.review.dto.ReviewDto;
import dev.sodev.domain.review.service.ReviewService;
import dev.sodev.global.Response;
import dev.sodev.domain.member.dto.MemberInfo;
import dev.sodev.domain.member.dto.UpdatePassword;
import dev.sodev.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member", description = "회원 api")
@RequiredArgsConstructor
@RequestMapping("/v1")
@RestController
public class MemberController {

    private final MemberService memberService;
    private final ProjectService projectService;
    private final ReviewService reviewService;

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임, 핸드폰 번호를 입력하여 회원가입을 요청합니다.")
    @PostMapping("/join")
    public Response<MemberJoinResponse> join(@RequestBody @Valid MemberJoinRequest request) {
        MemberJoinResponse response = memberService.join(request);
        return Response.success(response);
    }

    @Operation(summary = "회원 정보", description = "회원의 id로 해당 회원의 정보를 요청합니다.")
    @GetMapping("/members/{memberId}")
    public Response<MemberInfo> memberInfo(@PathVariable Long memberId) {
        MemberInfo memberInfo = memberService.getMemberInfo(memberId);
        return Response.success(memberInfo);
    }

    @Operation(summary = "내 정보", description = "요청자 본인의 회원 정보를 요청합니다.")
    @GetMapping("/members")
    public Response<MemberInfo> myInfo() {
        MemberInfo memberInfo = memberService.getMyInfo();
        return Response.success(memberInfo);
    }

    @Operation(summary = "회원 정보 수정", description = "이메일, 닉네임, 핸드폰 번호, 자기소개를 입력하여 기존에 작성한 프로필 정보를 수정합니다.")
    @PatchMapping("/members")
    public Response<MemberUpdateResponse> updateInfo(@RequestBody @Valid MemberUpdateRequest request) {
        MemberUpdateResponse response = memberService.update(request);
        return Response.success(response);
    }

    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호, 바꾸고자 하는 비밀번호를 입력하여 비밀번호 변경을 요청합니다.")
    @PatchMapping("/members/password")
    public Response<MemberUpdateResponse> updatePassword(@RequestBody @Valid UpdatePassword updatePassword) {
        MemberUpdateResponse response = memberService.updatePassword(updatePassword);
        return Response.success(response);
    }

    @Operation(summary = "좋아요 누른 프로젝트 리스트", description = "회원의 id와 Pageable queryParam 으로 해당 회원이 좋아요 누른 프로젝트 리스트를 요청합니다.")
    @GetMapping("/members/{memberId}/likes") // 좋아요 누른 프로젝트 목록
    public Slice<LikesProjectDto> likeProjectList(@PathVariable Long memberId, Pageable pageable) {
        return projectService.likeProject(memberId, pageable);
    }

    @Operation(summary = "참여 지원한 프로젝트 리스트", description = "회원의 id와 Pageable queryParam 으로 해당 회원이 참여 지원한 프로젝트 리스트를 요청합니다.")
    @GetMapping("/members/{memberId}/applies") // 지원한 프로젝트 목록
    public Slice<MemberAppliedDto> applyProjectList(@PathVariable Long memberId, Pageable pageable) {
        return projectService.applyProject(memberId, pageable);
    }

    @Operation(summary = "완료한 프로젝트 리스트", description = "회원의 id와 Pageable queryParam 으로 해당 회원이 완료한 프로젝트 리스트를 요청합니다.")
    @GetMapping("/members/{memberId}/history") // 진행한 프로젝트 목록
    public Slice<MemberHistoryDto> projectHistoryList(@PathVariable Long memberId, Pageable pageable) {
        return projectService.projectHistory(memberId, pageable);
    }

    @Operation(summary = "동료평가 리스트", description = "회원의 id와 Pageable queryParam 으로 해당 회원이 받은 동료평가 리스트를 요청합니다.")
    @GetMapping("/members/{memberId}/review") // 회원 동료평가 목록
    public Slice<ReviewDto> getMemberReview(@PathVariable Long memberId, Pageable pageable) {
        return reviewService.getReviews(memberId, pageable);
    }

}

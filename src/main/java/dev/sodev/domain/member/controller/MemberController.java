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
import dev.sodev.global.Response;
import dev.sodev.domain.member.dto.MemberInfo;
import dev.sodev.domain.member.dto.UpdatePassword;
import dev.sodev.domain.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/v1")
@RestController
public class MemberController {

    private final MemberService memberService;
    private final ProjectService projectService;

    @PostMapping("/join")
    public Response<MemberJoinResponse> join(@RequestBody @Valid MemberJoinRequest request) {
        MemberJoinResponse response = memberService.join(request);
        return Response.success(response);
    }

    @GetMapping("/members/{memberId}")
    public Response<MemberInfo> memberInfo(@PathVariable Long memberId) {
        MemberInfo memberInfo = memberService.getMemberInfo(memberId);
        return Response.success(memberInfo);
    }

    @GetMapping("/members")
    public Response<MemberInfo> myInfo() {
        MemberInfo memberInfo = memberService.getMyInfo();
        return Response.success(memberInfo);
    }

    @PatchMapping("/members")
    public Response<MemberUpdateResponse> updateInfo(@RequestBody @Valid MemberUpdateRequest request) {
        MemberUpdateResponse response = memberService.update(request);
        return Response.success(response);
    }

    @PatchMapping("/members/password")
    public Response<MemberUpdateResponse> updatePassword(@RequestBody @Valid UpdatePassword updatePassword) {
        MemberUpdateResponse response = memberService.updatePassword(updatePassword);
        return Response.success(response);
    }

    @GetMapping("/members/{memberId}/likes") // 좋아요 누른 프로젝트 목록
    public Slice<LikesProjectDto> likeProjectList(@PathVariable Long memberId, Pageable pageable) {
        return projectService.likeProject(memberId, pageable);
    }

    @GetMapping("/members/{memberId}/applies") // 지원한 프로젝트 목록
    public Slice<MemberAppliedDto> applyProjectList(@PathVariable Long memberId, Pageable pageable) {
        return projectService.applyProject(memberId, pageable);
    }

    @GetMapping("/members/{memberId}/history") // 진행한 프로젝트 목록
    public Slice<MemberHistoryDto> projectHistoryList(@PathVariable Long memberId, Pageable pageable) {
        return projectService.projectHistory(memberId, pageable);
    }


}

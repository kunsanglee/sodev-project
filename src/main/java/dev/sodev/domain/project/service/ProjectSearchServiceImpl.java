package dev.sodev.domain.project.service;


import dev.sodev.domain.comment.dto.CommentDto;
import dev.sodev.domain.comment.repsitory.CommentRepository;
import dev.sodev.domain.enums.ProjectRole;
import dev.sodev.domain.enums.SearchType;
import dev.sodev.domain.likes.dto.LikesMemberDto;
import dev.sodev.domain.likes.dto.LikesProjectDto;
import dev.sodev.domain.likes.repository.LikeRepository;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.member.MemberProject;
import dev.sodev.domain.member.dto.MemberAppliedDto;
import dev.sodev.domain.member.dto.MemberHistoryDto;
import dev.sodev.domain.member.dto.MemberProjectDto;
import dev.sodev.domain.member.repository.MemberProjectRepository;
import dev.sodev.domain.member.repository.MemberRepository;
import dev.sodev.domain.project.dto.ProjectDto;
import dev.sodev.domain.project.dto.response.ProjectResponse;
import dev.sodev.domain.project.repository.ProjectSkillRepository;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import dev.sodev.global.security.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectSearchServiceImpl implements ProjectSearchService {

    private final MemberProjectRepository memberProjectRepository;
    private final ProjectSkillRepository projectSkillRepository;
    private final MemberRepository memberRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;


    @Transactional(readOnly = true)
    @Override
    public ProjectResponse selectProject(Long projectId) {
        ProjectDto projectDto = projectSkillRepository.findProject(projectId).orElseThrow(() -> new SodevApplicationException(ErrorCode.FEED_NOT_FOUND));
        List<LikesMemberDto> likesDtoList = likeRepository.likeList(projectId);
        List<CommentDto> commentDtoList = commentRepository.findAllByProject(projectId).stream()
                .map(CommentDto::fromEntity)
                .toList();
        List<MemberProject> memberProjects = memberProjectRepository.findAllByProjectId(projectId);

        Map<Boolean, List<MemberProjectDto>> result = getApplicantsAndNonApplicants(memberProjects);
        List<MemberProjectDto> teamMember = result.get(false); // teamMember 리스트
        List<MemberProjectDto> applicants = result.get(true); // applicant 리스트

        projectDto.addLikes(likesDtoList);
        projectDto.addComments(commentDtoList);
        projectDto.addMemberProjects(teamMember);
        projectDto.addApplicants(applicants);

        return ProjectResponse.of(projectDto);
    }

    @Transactional(readOnly = true)
    @Override
    public Slice<ProjectDto> searchProject(SearchType searchType, String keyword, List<String> skillSet, Pageable pageable) {
        // 키워드가 없을 경우 그냥 상태가 RECRUIT 인 프로젝트 최신작성순으로 반환.
        if (searchType.equals(SearchType.ALL) || keyword.isBlank() && skillSet == null) {
            return projectSkillRepository.searchAll(pageable);
        }

        return projectSkillRepository.search(searchType, keyword, skillSet, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Slice<LikesProjectDto> getLikeProjects(Long memberId, Pageable pageable) {
        getCurrentMember();
        getMemberById(memberId);
        return likeRepository.findLikedProjectsByMemberId(memberId, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Slice<MemberAppliedDto> getApplyProjects(Long memberId, Pageable pageable) {
        getCurrentMember();
        getMemberById(memberId);
        return memberProjectRepository.findAppliedProjectsByMemberId(memberId, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Slice<MemberHistoryDto> getHistoryProjects(Long memberId, Pageable pageable) {
        getCurrentMember();
        getMemberById(memberId);
        return memberProjectRepository.findHistoryProjectsByMemberId(memberId, pageable);
    }


    // 요청하는 회원이 존재하는지 확인.
    private Member getCurrentMember() {
        Member member = memberRepository.getReferenceByEmail(SecurityUtil.getMemberEmail());
        if(member == null) throw new SodevApplicationException(ErrorCode.UNAUTHORIZED_USER);
        return member;
    }

    // 요청 대상 회원이 존재하는지 확인.
    private Member getMemberById(Long memberId) {
        Member member = memberRepository.getReferenceById(memberId);
        if (member == null) throw new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND);
        return member;
    }

    // 지원자와 기존 팀원들을 partitioningBy 로 나눠서 지원자면 key 값으로 true, 지원자리스트, 팀원이면 key 값으로 false, 팀원 리스트가 Map 에 담겨 반환.
    public Map<Boolean, List<MemberProjectDto>> getApplicantsAndNonApplicants(List<MemberProject> memberProjects) {
        return memberProjects.stream()
                .map(MemberProjectDto::fromEntity)
                .collect(Collectors.partitioningBy(mp -> mp.role().getRole().equals(ProjectRole.Role.APPLICANT)));
    }
}

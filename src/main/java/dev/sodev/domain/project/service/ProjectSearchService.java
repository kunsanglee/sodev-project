package dev.sodev.domain.project.service;


import dev.sodev.domain.enums.SearchType;
import dev.sodev.domain.likes.dto.LikesProjectDto;
import dev.sodev.domain.member.dto.MemberAppliedDto;
import dev.sodev.domain.member.dto.MemberHistoryDto;
import dev.sodev.domain.project.dto.ProjectDto;
import dev.sodev.domain.project.dto.response.ProjectResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface ProjectSearchService {

    ProjectResponse selectProject(Long projectId);

    Slice<ProjectDto> searchProject(SearchType searchType, String keyword, List<String> skillSet, Pageable pageable);

    Slice<LikesProjectDto> getLikeProjects(Long memberId, Pageable pageable);

    Slice<MemberAppliedDto> getApplyProjects(Long memberId, Pageable pageable);

    Slice<MemberHistoryDto> getHistoryProjects(Long memberId, Pageable pageable);

}

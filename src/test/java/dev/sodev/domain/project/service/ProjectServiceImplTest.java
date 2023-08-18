
package dev.sodev.domain.project.service;

import dev.sodev.domain.alarm.repository.AlarmRepository;
import dev.sodev.domain.comment.repsitory.CommentRepository;
import dev.sodev.domain.enums.ProjectRole;
import dev.sodev.domain.likes.repository.LikeRepository;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.member.MemberProject;
import dev.sodev.domain.member.repository.MemberProjectRepository;
import dev.sodev.domain.member.repository.MemberRepository;
import dev.sodev.domain.project.Project;
import dev.sodev.domain.project.dto.requset.ProjectInfoRequest;
import dev.sodev.domain.project.dto.response.ProjectResponse;
import dev.sodev.domain.project.repository.ProjectRepository;
import dev.sodev.domain.project.repository.ProjectSkillRepository;
import dev.sodev.domain.review.repository.ReviewRepository;
import dev.sodev.domain.skill.repository.SkillRepository;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import dev.sodev.global.security.utils.SecurityUtil;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Sql("/insert_skills.sql")
@Slf4j
@Transactional
@SpringBootTest
class ProjectServiceImplTest {

    @MockBean SecurityContextHolder securityContextHolder;
    @MockBean SkillRepository skillRepository;
    @MockBean ProjectRepository projectRepository;
    @MockBean MemberProjectRepository memberProjectRepository;
    @MockBean ProjectSkillRepository projectSkillRepository;
    @MockBean MemberRepository memberRepository;
    @MockBean LikeRepository likeRepository;
    @MockBean CommentRepository commentRepository;
    @MockBean ReviewRepository reviewRepository;
    @MockBean AlarmRepository alarmRepository;
    @Autowired ProjectService projectService;
    @Autowired EntityManager em;

    @Mock Member member;

    private Member getMember(String email, String nickName) {
        return Member.builder()
                .id(1L)
                .email(email)
                .password("test1234!")
                .nickName(nickName)
                .phone("010-1234-1234")
                .build();
    }

    private Project getProject() {
        return Project.builder()
                .id(1L)
                .be(3)
                .fe(3)
                .title("테스트 프로젝트 title")
                .content("테스트 프로젝트 content")
                .startDate(LocalDateTime.of(2023, 7, 30, 0, 0))
                .endDate(LocalDateTime.of(2023, 8, 30, 0, 0))
                .recruitDate(LocalDateTime.of(2023, 7, 29, 0, 0))
                .build();
    }

    private ProjectInfoRequest getProjectInfoRequest(List<String> skills) {
        return ProjectInfoRequest.builder()
                .be(3)
                .fe(3)
                .startDate(LocalDateTime.of(2023, 7, 30, 0, 0))
                .endDate(LocalDateTime.of(2023, 8, 30, 0, 0))
                .recruitDate(LocalDateTime.of(2023, 7, 29, 0, 0))
                .title("테스트 프로젝트 title")
                .content("테스트 프로젝트 content")
                .skillSet(skills)
                .roleType("BE")
                .build();
    }

    private MemberProject getMemberProject(Member member, Project project) {
        return MemberProject.of(member, project, ProjectRole.setProjectRole(ProjectRole.Role.CREATOR, ProjectRole.RoleType.BE));
    }

    @Test
    @WithMockUser(value = "test@test.com")
    @DisplayName("회원이 프로젝트 피드 등록 요청하여 성공")
    void givenMember_whenCreateFeed_thenReturnFeedSaved() {
        // given
        Member member = getMember("test@test.com", "테스트닉네임");
        Project project = getProject();
        ProjectInfoRequest projectInfoRequest = getProjectInfoRequest(List.of("java", "kotlin", "node.js", "python", "c"));
        Project request = ProjectInfoRequest.of(projectInfoRequest, member);

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));
        when(memberRepository.getReferenceByEmail(anyString())).thenReturn(member);
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        // when
        ProjectResponse response = projectService.createProject(projectInfoRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.project().getTitle()).isEqualTo(projectInfoRequest.title());
    }

    @Test
    @WithMockUser(value = "test@test.com")
    @DisplayName("회원이 이미 프로젝트에 참여중이면 새로운 프로젝트 생성 실패")
    void givenMemberAndMembersProject_whenMemberCreateAnotherProject_thenThrowsError() {
        // given
        Member member = getMember("test@test.com", "테스트닉네임");
        Project existingProject = getProject();
        MemberProject exsistingMemberProject = getMemberProject(member, existingProject);
        ProjectInfoRequest projectInfoRequest = getProjectInfoRequest(List.of("java", "kotlin", "node.js", "python", "c"));

        member.getMemberProject().add(exsistingMemberProject);

        when(memberRepository.getReferenceByEmail(SecurityUtil.getMemberEmail())).thenReturn(member);

        // when & then
        SodevApplicationException exception = assertThrows(SodevApplicationException.class, () -> {
            projectService.createProject(projectInfoRequest);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ALREADY_IN_PROJECT);
    }

    @Test
    @WithMockUser(value = "test@test.com")
    @DisplayName("회원이 존재하지 않는 기술스택을 입력하여 프로젝트 등록 요청시 실패")
    public void givenMember_whenMemberCreateFeedWithWrongSkills_thenThrowsError() throws Exception {
        // given
        Member member = getMember("test@test.com", "테스트닉네임");
        ProjectInfoRequest projectInfoRequest = getProjectInfoRequest(List.of("wrong", "none", "skills"));

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));
        when(memberRepository.getReferenceByEmail(anyString())).thenReturn(member);

        // when & then
        SodevApplicationException exception = assertThrows(SodevApplicationException.class, () -> {
            projectService.createProject(projectInfoRequest);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SKILL_NOT_FOUND);
    }

//    @Test
//    @WithMockUser(value = "test@test.com")
//    @DisplayName("")
//    public void givenMember_whenMemberCreateFeedWithWrongSkills_thenThrowsError() throws Exception {
//        // given
//        Member member = getMember("test@test.com", "테스트닉네임");
//        ProjectInfoRequest projectInfoRequest = getProjectInfoRequest(List.of("wrong", "none", "skills"));
//
//        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));
//        when(memberRepository.getReferenceByEmail(anyString())).thenReturn(member);
//
//        // when & then
//        SodevApplicationException exception = assertThrows(SodevApplicationException.class, () -> {
//            projectService.createProject(projectInfoRequest);
//        });
//
//        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST);
//    }
}


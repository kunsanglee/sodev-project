package dev.sodev.domain.project.service;

import dev.sodev.domain.enums.SearchType;
import dev.sodev.domain.enums.SkillCode;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.member.MemberProject;
import dev.sodev.domain.member.repository.MemberProjectRepository;
import dev.sodev.domain.member.repository.MemberRepository;
import dev.sodev.domain.project.Project;
import dev.sodev.domain.project.dto.ProjectDto;
import dev.sodev.domain.project.dto.requset.ProjectInfoRequest;
import dev.sodev.domain.project.dto.response.ProjectResponse;
import dev.sodev.domain.project.repository.ProjectRepository;
import dev.sodev.domain.project.repository.ProjectSkillRepository;
import dev.sodev.domain.skill.repository.SkillRepository;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import dev.sodev.global.security.utils.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService{

    private final SkillRepository skillRepository;
    private final ProjectRepository projectRepository;
    private final MemberProjectRepository memberProjectRepository;

    private final ProjectSkillRepository projectSkillRepository;
    private final MemberRepository memberRepository;

    @Override
    public List<ProjectResponse> projectList() {
        return null;
    }

    @Override
    public ProjectResponse selectProject(Long projectId) {

        List<ProjectDto> project = projectSkillRepository.findProject(projectId);
        return ProjectResponse.of(project);
    }

    @Override
    public ProjectResponse createProject(ProjectInfoRequest request) {
        // 프로젝트를 작성하면 member_project , project, project_skill, skill 다 값이 들어가야함

        Member member = memberRepository.getReferenceByEmail(SecurityUtil.getMemberEmail());
        // 로그인 한 유저가 없다면
        if(member == null) throw new SodevApplicationException(ErrorCode.UNAUTHORIZED_USER);

        Project project = Project.of(request, member);

        Long saveProject = projectRepository.save(project).getId();
        memberProjectRepository.save(MemberProject.of(member, project));
        // request 의 skill 들이 없으면 저장 후 리스트로 반환
        List<Integer> skills = findAndSaveSkill(request.skillSet());

        // usage update
        skillRepository.bulkUsageUpdate(skills);
        projectSkillRepository.saveAll(skills, saveProject );

        return ProjectResponse.of("글 작성이 완료되었습니다.");
    }

    @Override
    public ProjectResponse updateProject(Long projectId,ProjectInfoRequest request) {
        // 로그인이 되어있지 않은경우 에러
        Member member = memberRepository.getReferenceByEmail(SecurityUtil.getMemberEmail());
        if(member == null) throw new SodevApplicationException(ErrorCode.UNAUTHORIZED_USER);
        // 프로젝트 피드가 존재하지 않는경우 에러반환
        Project project = projectRepository.findById(projectId).orElseThrow( () -> new SodevApplicationException(ErrorCode.FEED_NOT_FOUND));
        // 글을 작성한 유저와 수정하려는 유저가 다를경우 에러반환
        if(!SecurityUtil.getMemberEmail().equals(project.getCreatedBy())) throw new SodevApplicationException(ErrorCode.INVALID_PERMISSION);
        ProjectDto projectDto = ProjectDto.fromEntity(project);
        // TODO: 수정부분 setter 빼고 valid 따로 적용하려고 Project 클래스에 메서드 만들었는데 카 확인카 부탁
        project.setBe(request.be());
        project.setFe(request.fe());
        project.setTitle(request.title());
        project.setContent(request.content());
        project.setStartDate(request.start_date());
        project.setEndDate(request.end_date());
        project.setRecruitDate(request.recruit_date());

        projectRepository.save(project);
        List<Integer> skills = findAndSaveSkill(request.skillSet());
        skillRepository.bulkUsageUpdate(skills);
        projectSkillRepository.deleteAllByProjectId(projectId);
        projectSkillRepository.saveAll(skills, projectId );

        return ProjectResponse.of("글 수정이 완료되었습니다.");
    }

    @Override
    public ProjectResponse deleteProject(Long projectId) {
        Member member = memberRepository.getReferenceByEmail(SecurityUtil.getMemberEmail());
        if(member == null) throw new SodevApplicationException(ErrorCode.UNAUTHORIZED_USER);
        Project project = projectRepository.findById(projectId).orElseThrow( () -> new SodevApplicationException(ErrorCode.FEED_NOT_FOUND));
        if(!SecurityUtil.getMemberEmail().equals(project.getCreatedBy())) throw new SodevApplicationException(ErrorCode.INVALID_PERMISSION);
        // member_project, project, project_skill 다 삭제
        memberProjectRepository.deleteAllByProjectId(projectId);
        projectRepository.deleteById(projectId);
        projectSkillRepository.deleteAllByProjectId(projectId);
        return ProjectResponse.of("글 삭제가 완료되었습니다.");
    }

    @Override
    public List<Integer> findAndSaveSkill(List<String> skills) {
        return skills.stream().map(SkillCode::findSkillCode).toList();
    }

    @Override
    public Page<ProjectDto> searchProject(SearchType searchType, String keyword, List<String> skillSet, Pageable pageable) {
        // 키워드가 없을 경우 그냥 상태가 RECRUIT 인 프로젝트 최신작성순으로 반환
        if (keyword.isBlank() && skillSet.isEmpty()) {
            return projectSkillRepository.searchAll(pageable);
        }

        return switch (searchType) {
            case EMAIL -> projectSkillRepository.searchFromEmail(keyword,skillSet,pageable);
            case TITLE -> projectSkillRepository.searchFromTitle(keyword, skillSet,pageable);
            case CONTENT -> projectSkillRepository.searchFromContent(keyword,skillSet, pageable);
            case SKILL -> projectSkillRepository.searchFromSkill(skillSet, pageable);
            case NICKNAME -> projectSkillRepository.searchFromNickname(keyword,skillSet, pageable);
        };
    }

    @Override
    public Page<ProjectDto> likeProject(String userName, Pageable pageable) {
        return null;
    }

    @Override
    public Page<ProjectDto> offerProject(String userName) {
        return null;
    }

    @Override
    public Page<ProjectDto> applyProject(String userName, Pageable pageable) {
        return null;
    }

    @Override
    public Page<ProjectDto> projectHistory(String userName, Pageable pageable) {
        return null;
    }


}

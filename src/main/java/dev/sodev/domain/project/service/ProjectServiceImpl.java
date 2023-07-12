package dev.sodev.domain.project.service;

import dev.sodev.domain.member.Member;
import dev.sodev.domain.member.MemberProject;
import dev.sodev.domain.member.repository.MemberProjectRepository;
import dev.sodev.domain.member.repository.MemberRepository;
import dev.sodev.domain.project.Project;
import dev.sodev.domain.project.dto.projectDTO;
import dev.sodev.domain.project.dto.requset.ProjectInfoRequest;
import dev.sodev.domain.project.dto.requset.ProjectRequest;
import dev.sodev.domain.project.dto.response.ProjectResponse;
import dev.sodev.domain.project.repository.ProjectRepository;
import dev.sodev.domain.project.repository.ProjectSkillRepository;
import dev.sodev.domain.skill.Skill;
import dev.sodev.domain.skill.repository.SkillRepository;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public ProjectResponse selectProject(ProjectRequest projectRequest) {
        Long projectId = projectRequest.projectId();

        List<projectDTO> project = projectSkillRepository.findProject(projectId);


        return ProjectResponse.of(project);
    }

    @Override
    public ProjectResponse createProject(ProjectInfoRequest request) {
        // 프로젝트를 작성하면
        // 1. member_project , project, project_skill, skill 다 값이 들어가야함
        // 임시
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Member member = memberRepository.getReferenceByEmail(authentication.getName());
        if(member == null) {
            throw new SodevApplicationException(ErrorCode.UNAUTHORIZED_USER);
        }
        Project project = Project.of(request);

        Long saveProject = projectRepository.save(project).getId();
        memberProjectRepository.save(MemberProject.of(member, project));
        // request 의 skill 들이 없으면 저장 후 리스트로 반환
        List<Skill> skills = findAndSaveSkill(request.skillSet());
        // usage update
        skillRepository.usagePlus(skills);
        projectSkillRepository.saveAll(skills, saveProject );

        return ProjectResponse.of("글 작성이 완료되었습니다.");
    }

    @Override
    public ProjectResponse updateProject(ProjectInfoRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Member member = memberRepository.getReferenceByEmail(authentication.getName());
        if(member == null) {
            throw new SodevApplicationException(ErrorCode.UNAUTHORIZED_USER);
        }
        Project project = projectRepository.findById(request.projectId()).orElseThrow( () -> new SodevApplicationException(ErrorCode.FEED_NOT_FOUND));
        project.setBe(request.be());
        project.setFe(request.fe());
        project.setTitle(request.title());
        project.setContent(request.content());
        project.setStartDate(request.start_date());
        project.setEndDate(request.end_date());
        project.setRecruitDate(request.recruit_date());

        projectRepository.save(project);
        List<Skill> skills = findAndSaveSkill(request.skillSet());
        skillRepository.usagePlus(skills);
        projectSkillRepository.deleteAllByProjectId(request.projectId());
        projectSkillRepository.saveAll(skills, project.getId() );

        return ProjectResponse.of("글 수정이 완료되었습니다.");
    }

    @Override
    public ProjectResponse deleteProject(ProjectRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Member member = memberRepository.getReferenceByEmail(authentication.getName());
        if(member == null) {
            throw new SodevApplicationException(ErrorCode.UNAUTHORIZED_USER);
        }
        // member_project, project, project_skill 다 삭제
        memberProjectRepository.deleteAllByProjectId(request.projectId());
        projectRepository.deleteById(request.projectId());
        projectSkillRepository.deleteAllByProjectId(request.projectId());
        return ProjectResponse.of("글 삭제가 완료되었습니다.");
    }

    @Override
    public List<Skill> findAndSaveSkill(List<String> skills) {
        return skills.stream().map( skill -> skillRepository.findSkillByName(skill).orElseGet( () -> skillRepository.save(Skill.of(skill)))).toList();
    }
}

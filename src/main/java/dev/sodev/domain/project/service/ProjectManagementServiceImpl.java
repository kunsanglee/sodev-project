package dev.sodev.domain.project.service;


import dev.sodev.domain.comment.repsitory.CommentRepository;
import dev.sodev.domain.enums.*;
import dev.sodev.domain.likes.repository.LikeRepository;
import dev.sodev.domain.project.dto.ProjectApplyDto;
import dev.sodev.global.kafka.AlarmProducer;
import dev.sodev.global.kafka.event.AlarmEvent;
import jakarta.persistence.EntityManager;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.member.MemberProject;
import dev.sodev.domain.member.dto.MemberProjectDto;
import dev.sodev.domain.member.repository.MemberProjectRepository;
import dev.sodev.domain.member.repository.MemberRepository;
import dev.sodev.domain.project.Project;
import dev.sodev.domain.project.dto.ProjectDto;
import dev.sodev.domain.project.dto.requset.PeerReviewRequest;
import dev.sodev.domain.project.dto.requset.ProjectInfoRequest;
import dev.sodev.domain.project.dto.response.ProjectResponse;
import dev.sodev.domain.project.repository.ProjectRepository;
import dev.sodev.domain.project.repository.ProjectSkillRepository;
import dev.sodev.domain.review.Review;
import dev.sodev.domain.review.repository.ReviewRepository;
import dev.sodev.domain.skill.repository.SkillRepository;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import dev.sodev.global.security.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectManagementServiceImpl implements ProjectManagementService {

    private final SkillRepository skillRepository;
    private final ProjectRepository projectRepository;
    private final MemberProjectRepository memberProjectRepository;
    private final ProjectSkillRepository projectSkillRepository;
    private final MemberRepository memberRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;
    private final AlarmProducer alarmProducer;
    private final EntityManager em;


    @Override
    public ProjectResponse createProject(ProjectInfoRequest request) {
        Member member = getCurrentMember();
        member.isAlreadyInProject();
        request.validateSkills();

        Project project = createNewProject(request, member);

        MemberProject memberProject = MemberProject.getMemberProject(request, member, project);
        addAndSaveMemberProject(member, project, memberProject);

        List<Integer> skills = findAndSaveSkill(request.skillSet());
        updateSkillsAndSaveProjectSkill(project, skills);

        List<Member> receivers = member.alarmsToFollower();
        sendNewProjectAlarm(receivers, AlarmType.FEED_CREATED, member, project);

        ProjectDto response = ProjectDto.fromEntity(project);

        return ProjectResponse.of(response);
    }

    @Override
    public ProjectResponse updateProject(Long projectId, ProjectInfoRequest request) {
        request.validateUpdateRequest();
        Member member = getCurrentMember();
        Project project = getProjectWithMembersById(projectId);
        member.isCreator(project);

        project.update(request);
        updateSkillAndProjectSkill(projectId, request);

        List<Member> receivers = project.alarmsToMember();
        sendNewProjectAlarm(receivers, AlarmType.FEED_UPDATED, member, project);

        return ProjectResponse.of("글 수정이 완료되었습니다.");
    }

    @Override
    public ProjectResponse deleteProject(Long projectId) {
        Member member = getCurrentMember();
        Project project = getProjectById(projectId);
        member.isCreator(project);

        // memberProject, comments, likes, skills 다 bulk delete 하고 삭제.
        bulkDeleteMemberProject(projectId);
        bulkDeleteComment(projectId);
        bulkDeleteLikes(projectId);
        bulkDeleteProjectSkill(projectId);
        em.flush();
        em.clear();

        projectRepository.delete(project);

        return ProjectResponse.of("글 삭제가 완료되었습니다.");
    }

    // request 의 skill 들이 없으면 저장 후 리스트로 반환.
    @Override
    public List<Integer> findAndSaveSkill(List<String> skills) {
        return skills.stream().map(SkillCode::findSkillCode).toList();
    }

    @Override
    public void applyProject(Long projectId, ProjectApplyDto applyDto) { // 프로젝트 참여 지원
        Member applicant = getCurrentMember();
        applicant.isAlreadyInProject();
        Project project = getProjectById(projectId);

        String type = applyDto.roleType();
        ProjectRole.RoleType roleType = ProjectRole.getRoleType(type);

        MemberProject memberProject = MemberProject.of(applicant, project, ProjectRole.setProjectRole(ProjectRole.Role.APPLICANT, roleType));
        memberProject.addProjectAndApplicant(applicant, project); // 프로젝트의 지원자리스트, 회원의 지원한 프로젝트 리스트에 추가.

        memberProjectRepository.save(memberProject);

        // 누군가 프로젝트에 참여 요청을 하면 리스트에 추가되고, 해당 프로젝트 참여 인원에게 카프카 알림을 보냄.
        List<Member> receivers = project.alarmsToMember();
        sendNewProjectAlarm(receivers, AlarmType.APPLICANT_ON_FEED, applicant, project);
    }

    @Override
    public void acceptApplicant(Long projectId, MemberProjectDto memberProjectDto) {
        Project project = getProjectById(projectId);
        project.isJoinable(memberProjectDto);
        Member member = getCurrentMember();
        member.isCreator(project);
        Member applicant = getMemberById(memberProjectDto.memberId());

        // 지원자가 현재 진행중이거나 생성한 프로젝트가 있는지 확인.
        applicant.isAlreadyInProject();
        MemberProject applicantMemberProject = project.getApplicantMemberProject(applicant);
        applicantMemberProject.updateRole(ProjectRole.setProjectRole(ProjectRole.Role.MEMBER, memberProjectDto.role().getRoleType()));

        // 해당 project 에 지원자 합류.
        applicantMemberProject.addProjectAndMember(applicant, project);

        // 팀에 합류한 지원자의 다른 지원들 모두 삭제.
        applicantMemberProject.deleteProjectApplicant(applicant, project);
        memberProjectRepository.deleteAllByApplicantId(applicant.getId());

        // 프로젝트 구성원들과 합류된 지원자에게 알림 발송.
        List<Member> receivers = project.alarmsToMember();
        sendNewProjectAlarm(receivers, AlarmType.NEW_MEMBER_JOINED, member, project);
    }

    @Override
    public void declineApplicant(Long projectId, MemberProjectDto memberProjectDto) {
        Member member = getCurrentMember();
        Project project = getProjectById(projectId);
        member.isCreator(project);
        Member applicant = getMemberById(memberProjectDto.memberId());
        MemberProject memberProject = project.getApplicantMemberProject(applicant);

        // 거절당한 지원자의 지원 삭제.
        memberProject.deleteProjectApplicant(applicant, project);

        memberProjectRepository.delete(memberProject); // MemberProject 테이블에서 데이터 삭제.

        // 거절된 지원자에게 거절 알림 발송 추가. -> kafka produce
        alarmProducer.send(AlarmEvent.of(AlarmType.TEAM_JOIN_FAILED, member, project, List.of(applicant)));
    }

    @Override
    public void kickMember(Long projectId, MemberProjectDto memberProjectDto) {
        // 내보낼 회원의 역할이 작성자면 퇴장시킬 수 없음.
        memberProjectDto.isRoleCreator();

        Member member = getCurrentMember();
        Project project = getProjectById(projectId);
        member.isCreator(project);

        MemberProject memberProject = project.getMemberProject(member);
        ProjectRole role = memberProject.getProjectRole();

        // 프로젝트 구성원들에게 퇴장되는 회원의 퇴장 알림 발송 추가.
        List<Member> receivers = project.alarmsToMember();
        sendNewProjectAlarm(receivers, AlarmType.MEMBER_KICKED_OUT, member, memberProject.getProject());

        // 내보낼 회원의 MemberProject 삭제.
        memberProjectRepository.deleteByProject_IdAndMember_Id(projectId, memberProjectDto.memberId());
    }

    @Override
    public void evaluationMembers(Long projectId, Long memberId, PeerReviewRequest request) {
        Member member = getCurrentMember();

        // 평가 대상의 memberProject 가 없을 경우 에러반환.
        MemberProject targetMemberProject = memberProjectRepository.findAllByProjectId(projectId).stream()
                .filter(mp -> mp.getMember().getId().equals(memberId))
                .findAny()
                .orElseThrow(() -> new SodevApplicationException(ErrorCode.BAD_REQUEST));
        Member targetMember = targetMemberProject.getMember();

        // 평가자의 memberProject 조회.
        Project targetProject = targetMemberProject.getProject();
        MemberProject writerMemberProject = targetProject.getMemberProject(member);

        // 평가자와 평가 대상 회원이 진행한 프로젝트의 id 가 일치하지 않는 경우 에러반환.
        Project writerProject = writerMemberProject.getProject();

        // 평가를 진행하는 조건에 부합하는지 확인.
        targetProject.isEvaluationAvailable(writerProject);

        // 평가 대상 회원의 리뷰 저장.
        reviewRepository.save(Review.of(targetMember, request.review()));

        // 평가 알림 저장. -> kafka produce
        List<Member> receivers = writerProject.alarmsToMember();
        sendNewProjectAlarm(receivers, AlarmType.PEER_REVIEW_CREATED, member, writerProject);
    }

    @Override
    public void startProject(Long projectId) {
        Member member = getCurrentMember();
        Project project = getProjectById(projectId);
        member.isCreator(project);

        // 프로젝트의 상태를 모집중 -> 진행중으로 변경.
        project.startProject();

        // 프로젝트 구성원들과 프로젝트 피드를 좋아요 누른 회원들에게 프로젝트 시작 알림 추가.
        List<Member> receiver = Stream.concat(
                project.alarmsToMember().stream(),
                project.alarmsToLikes().stream())
                .distinct()
                .toList();

        alarmProducer.send(AlarmEvent.of(AlarmType.PROJECT_STARTED, member, project, receiver));
    }

    @Override
    public void completeProject(Long projectId) {
        Member member = getCurrentMember();
        Project project = getProjectById(projectId);
        member.isCreator(project);

        // 프로젝트 상태를 진행중 -> 완료로 변경.
        project.completeProject();

        // 프로젝트 구성원들에게 프로젝트 종료, 상호평가 진행 요청 알림 추가.
        List<Member> receivers = Stream.concat(
                project.alarmsToMember().stream(),
                project.alarmsToLikes().stream())
                .distinct()
                .toList();

        sendNewProjectAlarm(receivers, AlarmType.PROJECT_COMPLETED, member, project);
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

    // 프로젝트 피드가 존재하지 않는경우 에러반환.
    private Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId).orElseThrow(() -> new SodevApplicationException(ErrorCode.FEED_NOT_FOUND));
    }

    // 프로젝트 피드와 프로젝트에 속해있는 팀원, 지원자 같이 조회.
    private Project getProjectWithMembersById(Long projectId) {
        return projectRepository.findByIdWithMembers(projectId).orElseThrow(() -> new SodevApplicationException(ErrorCode.FEED_NOT_FOUND));
    }

    // 프로젝트 생성.
    private Project createNewProject(ProjectInfoRequest request, Member member) {
        // ProjectInfoRequest -> Project
        Project project = ProjectInfoRequest.toEntity(request, member);

        // 프로젝트 저장.
        Project savedProject = projectRepository.save(project);

        return project;
    }

    // memberProject 연관관계 메서드 호출, memberProject 저장.
    private void addAndSaveMemberProject(Member member, Project project, MemberProject memberProject) {
        memberProject.addProjectAndMember(member, project);
        memberProjectRepository.save(memberProject);
    }

    // skill 을 추가하고 projectSkill 을 저장.
    private void updateSkillsAndSaveProjectSkill(Project savedProject, List<Integer> skills) {
        skillRepository.bulkUsageUpdate(skills);
        projectSkillRepository.saveAll(skills, savedProject.getId());
    }

    // 스킬셋이 null이 아니고 비어있지 않은 경우에만 처리.
    private void updateSkillAndProjectSkill(Long projectId, ProjectInfoRequest request) {
        if (request.skillSet() != null && !request.skillSet().isEmpty()) {
            request.validateSkills();
            List<Integer> skills = findAndSaveSkill(request.skillSet());
            skillRepository.bulkUsageUpdate(skills);
            bulkDeleteProjectSkill(projectId);
            em.flush();
            em.clear();

            projectSkillRepository.saveAll(skills, projectId);
        }
    }

    // 알림 대상 회원에게 프로젝트 알림 발송.
    private void sendNewProjectAlarm(List<Member> receivers, AlarmType alarmType, Member member, Project project) {
        alarmProducer.send(AlarmEvent.of(alarmType, member, project, receivers));
    }

    // bulk method.
    private void bulkDeleteProjectSkill(Long projectId) { projectSkillRepository.deleteAllByProjectId(projectId); }

    private void bulkDeleteLikes(Long projectId) {
        likeRepository.deleteAllByProjectId(projectId);
    }

    private void bulkDeleteComment(Long projectId) {
        commentRepository.deleteAllByProjectId(projectId);
    }

    private void bulkDeleteMemberProject(Long projectId) {
        memberProjectRepository.deleteAllByProjectId(projectId);
    }
}

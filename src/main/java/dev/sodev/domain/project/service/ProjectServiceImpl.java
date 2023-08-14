package dev.sodev.domain.project.service;


import dev.sodev.domain.alarm.service.AlarmService;
import dev.sodev.domain.comment.repsitory.CommentRepository;
import dev.sodev.domain.enums.*;
import dev.sodev.domain.likes.dto.LikesProjectDto;
import dev.sodev.domain.likes.repository.LikeRepository;
import dev.sodev.domain.member.dto.MemberAppliedDto;
import dev.sodev.domain.member.dto.MemberHistoryDto;
import dev.sodev.domain.project.dto.ProjectApplyDto;
import dev.sodev.global.kafka.AlarmProducer;
import dev.sodev.global.kafka.event.AlarmEvent;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Pageable;
import dev.sodev.domain.comment.dto.CommentDto;
import dev.sodev.domain.likes.dto.LikesMemberDto;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.member.MemberProject;
import dev.sodev.domain.member.dto.MemberProjectDto;
import dev.sodev.domain.member.repository.MemberProjectRepository;
import dev.sodev.domain.member.repository.MemberRepository;
import dev.sodev.domain.project.Project;
import dev.sodev.domain.project.dto.ProjectDto;
import dev.sodev.domain.project.dto.requset.PeerReviewRequest;
import dev.sodev.domain.project.dto.requset.ProjectInfoRequest;
import dev.sodev.domain.project.dto.response.ProjectListResponse;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final SkillRepository skillRepository;
    private final ProjectRepository projectRepository;
    private final MemberProjectRepository memberProjectRepository;
    private final ProjectSkillRepository projectSkillRepository;
    private final MemberRepository memberRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;
    private final AlarmService alarmService;
    private final AlarmProducer alarmProducer;

    public static final String BE = "BE";
    public static final String FE = "FE";


    @Transactional(readOnly = true)
    @Override
    public ProjectListResponse projectList() {
//        프로젝트 리스트 구현해야함.
        return ProjectListResponse.of(new ArrayList<>(null));
    }

    @Transactional(readOnly = true)
    @Override
    public ProjectResponse selectProject(Long projectId) {
        ProjectDto projectDto = projectSkillRepository.findProject(projectId).orElseThrow(() -> new SodevApplicationException(ErrorCode.FEED_NOT_FOUND));
        List<LikesMemberDto> likesDtos = likeRepository.likeList(projectId);
        List<CommentDto> commentDtos = commentRepository.findAllByProject(projectId).stream()
                .map(CommentDto::of)
                .toList();
        List<MemberProject> memberProjects = memberProjectRepository.findAllByProjectId(projectId);

        Map<Boolean, List<MemberProjectDto>> result = getApplicantsAndNonApplicants(memberProjects);
        List<MemberProjectDto> teamMember = result.get(false); // teamMember 리스트
        List<MemberProjectDto> applicants = result.get(true); // applicant 리스트

        projectDto.addLikes(likesDtos);
        projectDto.addComments(commentDtos);
        projectDto.addMemberProjects(teamMember);
        projectDto.addApplicants(applicants);

        return ProjectResponse.of(projectDto);
    }

    @Override
    public ProjectResponse createProject(ProjectInfoRequest request) {
        Member member = getCurrentMember();
        isAlreadyInProject(member);
        validateSkills(request);

        Project project = createNewProject(request, member);
        ProjectDto response = ProjectDto.of(project);

        return ProjectResponse.of(response);
    }

    @Override
    public ProjectResponse updateProject(Long projectId, ProjectInfoRequest request) {
        Member member = getCurrentMember();
        Project project = getProjectById(projectId);
        isCreator(member, project);
        validateSkills(request);

        project.update(request);

        List<Integer> skills = findAndSaveSkill(request.skillSet());
        skillRepository.bulkUsageUpdate(skills);
        projectSkillRepository.deleteAllByProjectId(projectId);
        projectSkillRepository.saveAll(skills, projectId);

        sendNewProjectAlarm(alarmService.alarmsToMember(project), AlarmType.FEED_UPDATED, member, project);

        return ProjectResponse.of("글 수정이 완료되었습니다.");
    }

    @Override
    public ProjectResponse deleteProject(Long projectId) {
        Member member = getCurrentMember();
        Project project = getProjectById(projectId);
        isCreator(member, project);

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

    @Transactional(readOnly = true)
    @Override
    public Slice<ProjectDto> searchProject(SearchType searchType, String keyword, List<String> skillSet, Pageable pageable) {
        // 키워드가 없을 경우 그냥 상태가 RECRUIT 인 프로젝트 최신작성순으로 반환
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

    @Override
    public void applyProject(Long projectId, ProjectApplyDto applyDto) { // 프로젝트 참여 지원
        Member applicant = getCurrentMember();
        isAlreadyInProject(applicant);
        Project project = getProjectById(projectId);

        String type = applyDto.roleType();
        ProjectRole.RoleType role = getRoleType(type);

        MemberProject memberProject = MemberProject.of(applicant, project, ProjectRole.applicantOf(role));
        memberProject.addProjectAndApplicant(applicant, project); // 프로젝트의 지원자리스트, 회원의 지원한 프로젝트 리스트에 추가.

        memberProjectRepository.save(memberProject);

        // 누군가 프로젝트 지원 요청을 하면 리스트에 추가되고, 해당 프로젝트 참여 인원에게 카프카 알림을 보냄.
        log.info("프로젝트 참여인원 알림 저장");
        sendNewProjectAlarm(alarmService.alarmsToMember(project), AlarmType.APPLICANT_ON_FEED, applicant, project);
    }

    @Override
    public void acceptApplicant(Long projectId, MemberProjectDto memberProjectDto) {
        Member member = getCurrentMember();
        Project project = getProjectById(projectId);
        isCreator(member, project);
        Member applicant = getMemberById(memberProjectDto.memberId());
        MemberProject applicantMemberProject = getApplicantMemberProject(project, applicant);
        isJoinable(memberProjectDto, project, applicantMemberProject);

        // 지원자가 현재 진행중이거나 생성한 프로젝트가 있는지 확인.
        isAlreadyInProject(applicant);
        applicantMemberProject.updateRole(ProjectRole.memberOf(memberProjectDto.role().getRoleType()));

        // 해당 project 에 지원자 합류.
        applicantMemberProject.addProjectAndMember(applicant, project);
        log.info("프로젝트에 지원자 {} 합류", applicant.getNickName());

        // 팀에 합류한 지원자의 다른 지원들 모두 삭제
        applicantMemberProject.deleteProjectApplicant(applicant, project);
        memberProjectRepository.deleteAllByApplicantId(applicant.getId());

        // 프로젝트 구성원들과 합류된 지원자에게 알림 발송 추가해야됨.
        sendNewProjectAlarm(alarmService.alarmsToMember(project), AlarmType.NEW_MEMBER_JOINED, member, project);
    }

    @Override
    public void declineApplicant(Long projectId, MemberProjectDto memberProjectDto) {
        Member member = getCurrentMember();
        Project project = getProjectById(projectId);
        isCreator(member, project);
        Member applicant = getMemberById(memberProjectDto.memberId());
        MemberProject memberProject = getApplicantMemberProject(project, applicant);

        // 거절당한 지원자의 지원 삭제.
        memberProject.deleteProjectApplicant(applicant, project);

        memberProjectRepository.delete(memberProject); // MemberProject 테이블에서 데이터 삭제.
        log.info("참여자 {}({})의 지원 삭제 완료", applicant.getNickName(), applicant.getId());

        // 거절된 지원자에게 거절 알림 발송 추가. -> kafka produce
        alarmProducer.send(AlarmEvent.of(AlarmType.TEAM_JOIN_FAILED, member, project, List.of(applicant)));
    }

    @Override
    public void kickMember(Long projectId, MemberProjectDto memberProjectDto) {
        // 내보낼 회원의 역할이 작성자면 퇴장시킬 수 없음.
        if (memberProjectDto.role().getRole().equals(ProjectRole.Role.CREATOR)) {
            throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "프로젝트 주인은 퇴장시킬 수 없습니다.");
        }

        Member member = getCurrentMember();
        Project project = getProjectById(projectId);
        isCreator(member, project);

        MemberProject memberProject = getMemberProject(member);
        ProjectRole role = memberProject.getProjectRole();

        // 프로젝트 구성원들에게 퇴장되는 회원의 퇴장 알림 발송 추가.
        sendNewProjectAlarm(alarmService.alarmsToMember(memberProject.getProject()), AlarmType.MEMBER_KICKED_OUT, member, memberProject.getProject());

        // 내보낼 회원의 MemberProject 삭제 -> cascade 로 인해 회원과 프로젝트 리스트에서도 삭제됨.
        memberProjectRepository.deleteByProject_IdAndMember_Id(projectId, memberProjectDto.memberId());
    }

    @Override
    public void evaluationMembers(Long projectId, Long memberId, PeerReviewRequest request) {
        Member member = getCurrentMember();

        // 평가 대상의 memberProject 가 없을 경우 에러반환
        MemberProject targetMemberProject = memberProjectRepository.findAllByProjectId(projectId).stream()
                .filter(mp -> mp.getMember().getId().equals(memberId))
                .findAny()
                .orElseThrow(() -> new SodevApplicationException(ErrorCode.BAD_REQUEST));

        // 평가자의 memberProject 조회
        MemberProject writerMemberProject = getMemberProject(member);

        // 평가자와 평가 대상 회원이 진행한 프로젝트의 id 가 일치하지 않는 경우 에러반환.
        Project writerProject = writerMemberProject.getProject();

        isEvaluationAvailable(targetMemberProject, writerProject);

        // 평가 대상 회원의 리뷰 저장.
        reviewRepository.save(Review.of(targetMemberProject.getMember(), request.review()));

        // 평가 알림 저장. -> kafka produce
        sendNewProjectAlarm(alarmService.alarmsToMember(writerProject), AlarmType.PEER_REVIEW_CREATED, member, writerProject);
    }

    @Override
    public void startProject(Long projectId) {
        Member member = getCurrentMember();
        Project project = getProjectById(projectId);
        isCreator(member, project);

        // 프로젝트의 상태를 모집중 -> 진행중으로 변경
        project.startProject();

        // 프로젝트 구성원들과 프로젝트 피드를 좋아요 누른 회원들에게 프로젝트 시작 알림 추가.
        List<Member> receiver = Stream.concat(
                alarmService.alarmsToMember(project).stream(),
                alarmService.alarmsToLikes(project).stream()).distinct().toList();

        alarmProducer.send(AlarmEvent.of(AlarmType.PROJECT_STARTED, member, project, receiver));
    }

    @Override
    public void completeProject(Long projectId) {
        Member member = getCurrentMember();
        Project project = getProjectById(projectId);
        isCreator(member, project);

        // 프로젝트 상태를 진행중 -> 완료로 변경
        project.completeProject();

        // 프로젝트 구성원들에게 프로젝트 종료, 상호평가 진행 요청 알림 추가.
        sendNewProjectAlarm(Stream.concat(
                        alarmService.alarmsToMember(project).stream(),
                        alarmService.alarmsToLikes(project).stream()).distinct().toList(),
                AlarmType.PROJECT_COMPLETED, member, project);
    }


    // 요청하는 회원이 존재하는지 확인
    private Member getCurrentMember() {
        Member member = memberRepository.getReferenceByEmail(SecurityUtil.getMemberEmail());
        if(member == null) throw new SodevApplicationException(ErrorCode.UNAUTHORIZED_USER);
        return member;
    }

    // 요청 대상 회원이 존재하는지 확인
    private Member getMemberById(Long memberId) {
        Member member = memberRepository.getReferenceById(memberId);
        if (member == null) throw new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND);
        return member;
    }

    // 프로젝트 피드가 존재하지 않는경우 에러반환
    private Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId).orElseThrow(() -> new SodevApplicationException(ErrorCode.FEED_NOT_FOUND));
    }

    // Member 의 MemberProject 존재하는지 확인
    private MemberProject getMemberProject(Member member) {
        MemberProject memberProject = memberProjectRepository.getReferenceByMemberId(member.getId());
        if (memberProject == null) throw new SodevApplicationException(ErrorCode.BAD_REQUEST);
        return memberProject;
    }

    // 지원자와 기존 팀원들을 partitioningBy 로 나눠서 지원자면 key 값으로 true, 지원자리스트, 팀원이면 key 값으로 false, 팀원 리스트가 Map 에 담겨 반환.
    public Map<Boolean, List<MemberProjectDto>> getApplicantsAndNonApplicants(List<MemberProject> memberProjects) {
        return memberProjects.stream()
                .map(MemberProjectDto::of)
                .collect(Collectors.partitioningBy(mp -> mp.role().getRole().equals(ProjectRole.Role.APPLICANT)));
    }

    // 작성자와 요청자가 다를경우 에러반환
    private static void isCreator(Member member, Project project) {
        if (!project.getCreatedBy().equals(member.getEmail())) throw new SodevApplicationException(ErrorCode.INVALID_PERMISSION);
    }

    // 요청하는 회원이 이미 진행중이거나, 참여중인 프로젝트가 있는지 확인
    private static void isAlreadyInProject(Member member) {
        member.getMemberProject().stream()
                .map(MemberProject::getProjectRole)
                .filter(pr -> pr.getRole().equals(ProjectRole.Role.CREATOR) || pr.getRole().equals(ProjectRole.Role.MEMBER))
                .findAny()
                .ifPresent(state -> {
                    throw new SodevApplicationException(ErrorCode.ALREADY_IN_PROJECT);
                });
    }

    // 해당 프로젝트의 직무 자리가 남아있는지 확인
    private static void isJoinable(MemberProjectDto memberProjectDto, Project project, MemberProject memberProject) {
        long size = project.getMembers().stream()
                .filter(m -> m.getProjectRole().getRoleType().equals(memberProjectDto.role().getRoleType()))
                .count();

        if (memberProject.getProjectRole().getRoleType().equals(ProjectRole.RoleType.BE)) { // 백앤드인 경우
            if (project.getBe() - size <= 0) {
                throw new SodevApplicationException(ErrorCode.RECRUITMENT_EXCEED);
            }
        } else { // 프론트앤드인 경우
            if (project.getFe() - size <= 0) {
                throw new SodevApplicationException(ErrorCode.RECRUITMENT_EXCEED);
            }
        }
    }

    // 동료평가시 평가하려는 회원의 프로젝트가 완료된 상태인지, 같은 프로젝트를 진행했는지 확인
    private static void isEvaluationAvailable(MemberProject targetMemberProject, Project writerProject) {
        if (!writerProject.getState().equals(ProjectState.COMPLETE) ||
                !writerProject.getId().equals(targetMemberProject.getProject().getId())) {
            throw new SodevApplicationException(ErrorCode.BAD_REQUEST);
        }
    }

    // roleType 추출 메서드
    private static ProjectRole.RoleType getRoleType(String type) {
        ProjectRole.RoleType role;
        if (type.equals(BE)) {
            role = ProjectRole.RoleType.BE;
        } else if (type.equals(FE)) {
            role = ProjectRole.RoleType.FE;
        } else {
            throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "BE 또는 FE 둘 중 하나를 선택해주세요");
        }
        return role;
    }

    // 프로젝트 생성
    private Project createNewProject(ProjectInfoRequest request, Member member) {
        // ProjectInfoRequest -> Project
        Project project = ProjectInfoRequest.of(request, member);

        // roleType 적용
        ProjectRole.RoleType roleType = getRoleType(request.roleType());

        // 프로젝트 저장
        Project savedProject = projectRepository.save(project);

        // memberProject 생성 후 member 와 project 연관관계 메서드 호출 후 memberProject 저장
        MemberProject memberProject = MemberProject.of(member, project, ProjectRole.creatorOf(roleType));
        addAndSaveMemberProject(member, project, memberProject);

        // request 의 skill 들이 없으면 저장 후 리스트로 반환
        List<Integer> skills = findAndSaveSkill(request.skillSet());

        // usage update
        updateSkillsAndSaveProjectSkill(savedProject, skills);

        // 프로젝트 생성자를 팔로잉 하는 회원들에게 알림 발송 추가
        sendNewProjectAlarm(alarmService.alarmsToFollower(member), AlarmType.FEED_CREATED, member, project);
        return project;
    }

    // memberProject 연관관계 메서드 호출, memberProject 저장
    private void addAndSaveMemberProject(Member member, Project project, MemberProject memberProject) {
        memberProject.addProjectAndMember(member, project);
        memberProjectRepository.save(memberProject);
    }

    // skill 을 추가하고 projectSkill 을 저장
    private void updateSkillsAndSaveProjectSkill(Project savedProject, List<Integer> skills) {
        skillRepository.bulkUsageUpdate(skills);
        projectSkillRepository.saveAll(skills, savedProject.getId());
    }

    // 알림 대상 회원에게 프로젝트 알림 발송
    private void sendNewProjectAlarm(List<Member> receivers, AlarmType alarmType, Member member, Project project) {
        alarmProducer.send(AlarmEvent.of(alarmType, member, project, receivers));
    }

    // 요청 skill 검증
    private static void validateSkills(ProjectInfoRequest request) {
        if (request.skillSet().stream().anyMatch(skill -> SkillCode.findSkillCode(skill) == null)) {
            throw new SodevApplicationException(ErrorCode.SKILL_NOT_FOUND, "목록에 없는 기술스택입니다.");
        }
    }

    // 지원자의 memberProject 반환
    private static MemberProject getApplicantMemberProject(Project project, Member applicant) {
        return project.getApplicants().stream()
                .filter(mp -> mp.getMember().equals(applicant))
                .findFirst()
                .orElseThrow(() -> new SodevApplicationException(ErrorCode.BAD_REQUEST));
    }
}

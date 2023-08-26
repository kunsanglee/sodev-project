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
import jakarta.persistence.EntityManager;
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

import java.time.LocalDateTime;
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

    public static final String BE = "BE";
    public static final String FE = "FE";

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
        member.isAlreadyInProject();
        validateSkills(request);

        Project project = createNewProject(request, member);
        ProjectDto response = ProjectDto.of(project);

        return ProjectResponse.of(response);
    }

    @Override
    public ProjectResponse updateProject(Long projectId, ProjectInfoRequest request) {
        validateUpdateRequest(request);
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
        projectRepository.delete(project);
        em.flush();
        em.clear();

        return ProjectResponse.of("글 삭제가 완료되었습니다.");
    }

    @Override
    public List<Integer> findAndSaveSkill(List<String> skills) {
        return skills.stream().map(SkillCode::findSkillCode).toList();
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

    @Override
    public void applyProject(Long projectId, ProjectApplyDto applyDto) { // 프로젝트 참여 지원
        Member applicant = getCurrentMember();
        applicant.isAlreadyInProject();
        Project project = getProjectById(projectId);

        String type = applyDto.roleType();
        ProjectRole.RoleType roleType = getRoleType(type);

        MemberProject memberProject = MemberProject.of(applicant, project, ProjectRole.setProjectRole(ProjectRole.Role.APPLICANT, roleType));
        memberProject.addProjectAndApplicant(applicant, project); // 프로젝트의 지원자리스트, 회원의 지원한 프로젝트 리스트에 추가.

        memberProjectRepository.save(memberProject);

        // 누군가 프로젝트 지원 요청을 하면 리스트에 추가되고, 해당 프로젝트 참여 인원에게 카프카 알림을 보냄.
        log.info("프로젝트 참여인원 알림 저장");
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
        member.isCreator(project);

        MemberProject memberProject = project.getMemberProject(member);
        ProjectRole role = memberProject.getProjectRole();

        // 프로젝트 구성원들에게 퇴장되는 회원의 퇴장 알림 발송 추가.
        List<Member> receivers = project.alarmsToMember();
        sendNewProjectAlarm(receivers, AlarmType.MEMBER_KICKED_OUT, member, memberProject.getProject());

        // 내보낼 회원의 MemberProject 삭제 -> cascade 로 인해 회원과 프로젝트 리스트에서도 삭제됨.
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

//     프로젝트 피드와 프로젝트에 속해있는 팀원, 지원자 같이 조회.
    private Project getProjectWithMembersById(Long projectId) {
        return projectRepository.findByIdWithMembers(projectId).orElseThrow(() -> new SodevApplicationException(ErrorCode.FEED_NOT_FOUND));
    }

    // 지원자와 기존 팀원들을 partitioningBy 로 나눠서 지원자면 key 값으로 true, 지원자리스트, 팀원이면 key 값으로 false, 팀원 리스트가 Map 에 담겨 반환.
    public Map<Boolean, List<MemberProjectDto>> getApplicantsAndNonApplicants(List<MemberProject> memberProjects) {
        return memberProjects.stream()
                .map(MemberProjectDto::of)
                .collect(Collectors.partitioningBy(mp -> mp.role().getRole().equals(ProjectRole.Role.APPLICANT)));
    }

    // roleType 추출 메서드.
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

    // 프로젝트 생성.
    private Project createNewProject(ProjectInfoRequest request, Member member) {
        // ProjectInfoRequest -> Project
        Project project = ProjectInfoRequest.of(request, member);

        // roleType 적용.
        ProjectRole.RoleType roleType = getRoleType(request.roleType());

        // 프로젝트 저장.
        Project savedProject = projectRepository.save(project);

        // memberProject 생성 후 member 와 project 연관관계 메서드 호출 후 memberProject 저장.
        MemberProject memberProject = MemberProject.of(member, project, ProjectRole.setProjectRole(ProjectRole.Role.CREATOR, roleType));
        addAndSaveMemberProject(member, project, memberProject);

        // request 의 skill 들이 없으면 저장 후 리스트로 반환.
        List<Integer> skills = findAndSaveSkill(request.skillSet());

        // usage update
        updateSkillsAndSaveProjectSkill(savedProject, skills);

        // 프로젝트 생성자를 팔로잉 하는 회원들에게 알림 발송 추가.
        List<Member> receivers = member.alarmsToFollower();
        sendNewProjectAlarm(receivers, AlarmType.FEED_CREATED, member, project);
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

    // 알림 대상 회원에게 프로젝트 알림 발송.
    private void sendNewProjectAlarm(List<Member> receivers, AlarmType alarmType, Member member, Project project) {
        alarmProducer.send(AlarmEvent.of(alarmType, member, project, receivers));
    }

    // 스킬셋이 null이 아니고 비어있지 않은 경우에만 처리.
    private void updateSkillAndProjectSkill(Long projectId, ProjectInfoRequest request) {
        if (request.skillSet() != null && !request.skillSet().isEmpty()) {
            validateSkills(request);
            List<Integer> skills = findAndSaveSkill(request.skillSet());
            skillRepository.bulkUsageUpdate(skills);
            bulkDeleteProjectSkill(projectId);
            projectSkillRepository.saveAll(skills, projectId);
            em.flush();
            em.clear();
        }
    }

    // 요청 skill 검증.
    private static void validateSkills(ProjectInfoRequest request) {
        if (request.skillSet().stream().anyMatch(skill -> SkillCode.findSkillCode(skill) == null)) {
            throw new SodevApplicationException(ErrorCode.SKILL_NOT_FOUND, "목록에 없는 기술스택입니다.");
        }
    }

    // ProjectInfoRequest 유효성 검증 메서드.
    private void validateUpdateRequest(ProjectInfoRequest request) {
        if (request.title() != null && request.title().trim().isEmpty()) throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "제목은 공백이 불가합니다.");
        if (request.content() != null && request.content().trim().isEmpty()) throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "내용을 입력해주세요.");
        if (request.be() != null && request.be() <= 0) throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "백엔드 인원수는 음수일 수 없습니다.");
        if (request.fe() != null && request.fe() <= 0) throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "프론트엔드 인원수는 음수일 수 없습니다.");
        if (request.startDate() != null && request.startDate().isBefore(LocalDateTime.now())) throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "프로젝트 시작일을 현재보다 과거로 지정할 수 없습니다.");
        if (request.endDate() != null && request.endDate().isBefore(LocalDateTime.now())) throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "프로젝트 종료일을 현재보다 과거로 지정할 수 없습니다.");
        if (request.recruitDate() != null && request.recruitDate().isBefore(LocalDateTime.now())) throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "프로젝트 모집기간을 현재보다 과거로 지정할 수 없습니다.");
    }

    // project 삭제할 때 사용되는 bulk delete 메서드.
    private void bulkDeleteProjectSkill(Long projectId) {
        projectSkillRepository.deleteAllByProjectId(projectId);
    }

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

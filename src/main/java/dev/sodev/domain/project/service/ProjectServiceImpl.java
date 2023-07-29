package dev.sodev.domain.project.service;

import dev.sodev.domain.comment.dto.CommentDto;
import dev.sodev.domain.comment.repsitory.CommentCustomRepository;
import dev.sodev.domain.enums.ProjectRole;
import dev.sodev.domain.enums.ProjectState;
import dev.sodev.domain.enums.SearchType;
import dev.sodev.domain.enums.SkillCode;
import dev.sodev.domain.likes.dto.LikesDto;
import dev.sodev.domain.likes.repository.LikeCustomRepository;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    private final LikeCustomRepository likeCustomRepository;
    private final CommentCustomRepository commentCustomRepository;

    private final ReviewRepository reviewRepository;

    @Override
    public ProjectListResponse projectList() {
//        TODO : 프로젝트 리스트 구현해야함.
        return ProjectListResponse.of(new ArrayList<>(null));
    }

    @Override
    public ProjectResponse selectProject(Long projectId) {
        ProjectDto projectDto = projectSkillRepository.findProject(projectId).orElseThrow(() -> new SodevApplicationException(ErrorCode.FEED_NOT_FOUND));

        List<LikesDto> likesDtos = likeCustomRepository.likeList(projectId);
        List<CommentDto> commentDtos = commentCustomRepository.findAllByProject(projectId).stream().map(CommentDto::of).toList();
        List<MemberProject> memberProjects = memberProjectRepository.findAllByProjectId(projectId);
        List<MemberProjectDto> memberProjectDtos = memberProjects.stream().filter(mp -> !mp.getRole().equals(ProjectRole.APPLICANT)).map(MemberProjectDto::of).toList();
        List<MemberProjectDto> applicants = memberProjects.stream().filter(mp -> mp.getRole().equals(ProjectRole.APPLICANT)).map(MemberProjectDto::of).toList();

        projectDto.addLikes(likesDtos);
        projectDto.addComments(commentDtos);
        projectDto.addMemberProjects(memberProjectDtos);
        projectDto.addApplicants(applicants);


        return ProjectResponse.of(projectDto);
    }

    @Override
    public ProjectResponse createProject(ProjectInfoRequest request) {
        // 프로젝트를 작성하면 member_project , project, project_skill, skill 다 값이 들어가야함

        Member member = checkMember();

        // 요청을 하는 회원이 진행중인(프로젝트 개설한 상태거나, 참여중인지) 프로젝트가 있는지 확인해서 있으면 에러 반환.
        isAlreadyInProject(member);

        if (request.skillSet().stream().anyMatch(skill -> SkillCode.findSkillCode(skill) == null)) {
            throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "목록에 없는 기술스택입니다.");
        }

        Project project = ProjectInfoRequest.of(request, member);

        Long saveProject = projectRepository.save(project).getId();
        MemberProject memberProject = MemberProject.of(member, project, ProjectRole.CREATOR);
        memberProject.addProjectAndMember(member, project);
        memberProjectRepository.save(memberProject);
        // request 의 skill 들이 없으면 저장 후 리스트로 반환
        List<Integer> skills = findAndSaveSkill(request.skillSet());

        // usage update
        skillRepository.bulkUsageUpdate(skills);
        projectSkillRepository.saveAll(skills, saveProject);

        return ProjectResponse.of("글 작성이 완료되었습니다.");
    }

    @Override
    public ProjectResponse updateProject(Long projectId, ProjectInfoRequest request) {
        // 로그인이 되어있지 않은경우 에러
        Member member = checkMember();

        // 프로젝트 피드가 존재하지 않는경우 에러반환
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new SodevApplicationException(ErrorCode.FEED_NOT_FOUND));
        // 글을 작성한 유저와 수정하려는 유저가 다를경우 에러반환
        if(!SecurityUtil.getMemberEmail().equals(project.getCreatedBy())) throw new SodevApplicationException(ErrorCode.INVALID_PERMISSION);

//        ProjectDto projectDto = ProjectDto.fromEntity(project);

        if (request.skillSet().stream().anyMatch(skill -> SkillCode.findSkillCode(skill) == null)) {
            throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "목록에 없는 기술스택입니다.");
        }

        project.update(request);

        projectRepository.save(project);
        List<Integer> skills = findAndSaveSkill(request.skillSet());
        skillRepository.bulkUsageUpdate(skills);
        projectSkillRepository.deleteAllByProjectId(projectId);
        projectSkillRepository.saveAll(skills, projectId );

        return ProjectResponse.of("글 수정이 완료되었습니다.");
    }

    @Override
    public ProjectResponse deleteProject(Long projectId) {
        Member member = checkMember();

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

//    @Override
//    public Page<ProjectDto> offerProject(String userName) {
//        return null;
//    }

    @Override
    public Page<ProjectDto> applyProject(String userName, Pageable pageable) {
        return null;
    }

    @Override
    public void applyProject(Long projectId) { // 프로젝트 참여 지원
        Member member = checkMember();
        isAlreadyInProject(member);

        // TODO : 프로젝트에 지원 요청을 하면 해당 프로젝트에 지원자목록으로 추가해야함.
        // 지원자목록을 어디에 만들것인가? -> 프로젝트의 필드로 지원자 리스트를 추가. -> 완료
        // 팀 내부적으로 회의를 한 후 지원자 리스트에 추가된 회원의 참여를 수락, 거절 하는 것은 작성자만 할 수 있게 체크. todo
        // 프로젝트 조회할때는 팀원이 아닌 요청자에게 지원자 리스트는 보여주지 않음. -> 조회할때 해당 프로젝트의 팀원인지 아닌지 체크 여부 추가해서 구분하여 반환. todo
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new SodevApplicationException(ErrorCode.FEED_NOT_FOUND));
        MemberProject memberProject = MemberProject.of(member, project, ProjectRole.APPLICANT);
        memberProject.addProjectAndApplicant(member, project); // 프로젝트의 지원자리스트, 회원의 지원한 프로젝트 리스트에 추가.
        memberProjectRepository.save(memberProject);
        log.info("member.memberProjects.size={}", member.getMemberProject().size());
        log.info("member.applies.size={}", member.getApplies().size());
        // 누군가 프로젝트 지원 요청을 하면 리스트에 추가되고, 해당 프로젝트 참여 인원에게 카프카 알림을 보냄.

    }

    @Override
    public Page<ProjectDto> projectHistory(String userName, Pageable pageable) {
        return null;
    }

    @Override
    public void evaluationMembers(Long memberId, PeerReviewRequest request) {
        // 프로젝트 글 작성자가 프로젝트 완료 후 프로젝트 참여자들에게 카프카 알림발송 -> 프로젝트완료 후 평가하기
        // 1. memberProject 에서 projectId = ? , role = Creator, Member
        // TODO : 임의로 memberId(평가하는 사람의 ID) 를 controller 에서 들어오는 변수로 지정함, 추후 카프카 알림 설정시 로직 보완필요 (기능만 구현)
        // TODO : 시큐리티에서 로그인안한 사용자 체크, 권한체크 해주는거 확인해야함
        // 진행중인 프로젝트가 없을 경우 에러반환
        MemberProject memberProject = memberProjectRepository.getReferenceByMemberId(memberId);
        if (memberProject == null) throw new SodevApplicationException(ErrorCode.FEED_NOT_FOUND);
        if (!memberProject.getProject().getState().equals(ProjectState.COMPLETE))
            throw new SodevApplicationException(ErrorCode.BAD_REQUEST);
        // 프로젝트 참여한 n명의 인원 중 본인 제외 나머지 사람들 평가해야됨
//        List<MemberProject> List = memberProjectRepository.findAllByMemberId(memberId);
        Member reviewMember = memberRepository.getReferenceById(memberId);
        reviewRepository.save(Review.of(reviewMember, request.ReviewComment()));
    }

    private Member checkMember() { // 요청하는 회원이 존재하는지 확인
        Member member = memberRepository.getReferenceByEmail(SecurityUtil.getMemberEmail());
        if(member == null) throw new SodevApplicationException(ErrorCode.UNAUTHORIZED_USER);
        return member;
    }

    private static void isAlreadyInProject(Member member) { // 요청하는 회원이 이미 진행중이거나, 참여중인 프로젝트가 있는지 확인
        member.getMemberProject().stream()
                .map(MemberProject::getRole)
                .filter(state -> state.equals(ProjectRole.CREATOR) || state.equals(ProjectRole.MEMBER))
                .findAny()
                .ifPresent(state -> {
                    throw new SodevApplicationException(ErrorCode.ALREADY_IN_PROJECT);
                });
    }

}

package dev.sodev.domain.comment.service;

import dev.sodev.domain.comment.Comment;
import dev.sodev.domain.comment.dto.request.CommentDeleteRequest;
import dev.sodev.domain.comment.dto.request.CommentRequest;
import dev.sodev.domain.comment.dto.response.CommentListResponse;
import dev.sodev.domain.comment.dto.response.CommentResponse;
import dev.sodev.domain.comment.repsitory.CommentRepository;
import dev.sodev.domain.comment.repsitory.query.CommentCustomRepository;
import dev.sodev.domain.comment.repsitory.query.CommentCustomRepositoryImpl;
import dev.sodev.domain.enums.ProjectState;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.member.repository.MemberRepository;
import dev.sodev.domain.project.Project;
import dev.sodev.domain.project.repository.ProjectRepository;
import dev.sodev.global.exception.SodevApplicationException;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@Slf4j
@Transactional
@SpringBootTest
class CommentServiceTest {

    SecurityContextHolder securityContextHolder;
    MemberRepository memberRepository;
    ProjectRepository projectRepository;
    CommentService commentService;
    CommentRepository commentRepository;
    CommentCustomRepository customCommentRepository;
    @Autowired EntityManager em;


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
                .content("테스트 프로젝트 게시물")
                .startDate(LocalDateTime.of(2023, 7, 30, 0, 0))
                .endDate(LocalDateTime.of(2023, 8, 30, 0, 0))
                .recruitDate(LocalDateTime.of(2023, 7, 29, 0, 0))
                .state(ProjectState.RECRUIT)
                .comments(new ArrayList<>())
                .build();
    }

    private Comment getComment(Long id, Member member, String content, Comment parent) {
        return Comment.builder()
                .id(id)
                .member(member)
                .content(content)
                .parent(parent)
                .build();
    }

    private CommentRequest getCommentRequest(Long id, Long parentId) {
        return CommentRequest.builder()
                .id(id)
                .parentId(parentId)
                .content("테스트 댓글~")
                .build();
    }

    private CommentDeleteRequest getCommentDeleteRequest(Long id) {
        return CommentDeleteRequest.builder().id(id).build();
    }

    @BeforeEach
    public void setup() {
        this.securityContextHolder = Mockito.mock(SecurityContextHolder.class);
        this.memberRepository = Mockito.mock(MemberRepository.class);
        this.projectRepository = Mockito.mock(ProjectRepository.class);
        this.commentRepository = Mockito.mock(CommentRepository.class);
        this.customCommentRepository = Mockito.mock(CommentCustomRepositoryImpl.class);
        this.commentService = new CommentServiceImpl(memberRepository, projectRepository, commentRepository);
    }

    @Test
    @WithMockUser
    public void givenProjectFeed_whenCreateComment_thenReturnSuccess() throws Exception {
        // given
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = getMember("test@test.com", "테스트닉네임");
        Project project = getProject();
        CommentRequest commentRequest = getCommentRequest(null, null);

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        // when
        CommentResponse response = commentService.createComment(1L, commentRequest);

        // then
        assertThat(response.comment().content()).isEqualTo("테스트 댓글~");
        assertThat(response.comment().nickName()).isEqualTo(member.getNickName());
        assertThat(response.comment().parentId()).isNull();
    }

    @Test
    @WithMockUser
    public void givenNoProjectFeed_whenCreateComment_thenReturnFeedNotFoundException() throws Exception {
        // given
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = getMember("test@test.com", "테스트닉네임");
        CommentRequest commentRequest = getCommentRequest(null, null);

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));

        // when
        SodevApplicationException exception = assertThrows(SodevApplicationException.class,
                () -> commentService.createComment(1L, commentRequest));

        // then
        assertThat(exception.getMessage()).isEqualTo("존재하지 않는 게시글 입니다.");
        assertThat(exception.getErrorCode().getStatus().value()).isEqualTo(404);
    }

    @Test
    @WithMockUser
    public void givenProjectFeedAndParentComment_whenCreateChildComment_thenReturnCommentOfParentChildRelationship() throws Exception {
        // given
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = getMember("test@test.com", "테스트닉네임");
        Project project = getProject();
        Comment comment1 = getComment(1L, member, "테스트 댓글", null);
        project.getComments().add(comment1);
        CommentRequest request = getCommentRequest(null, comment1.getId());

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(commentRepository.findById(comment1.getId())).thenReturn(Optional.of(comment1));

        // when
        CommentResponse comment2 = commentService.createComment(project.getId(), request);
        log.info("comment2.comment.parentId={}", comment2.comment().parentId());

        // then
        assertThat(project.getComments().size()).isEqualTo(2);
        assertThat(comment2.comment().content()).isEqualTo("테스트 댓글~");
        assertThat(comment2.comment().parentId()).isEqualTo(comment1.getId());
        assertThat(comment1.getChildren().size()).isEqualTo(1);
    }

    @Test
    @WithMockUser
    public void givenProjectFeedAndComment_whenUpdateComment_thenReturnUpdatedComment() throws Exception {
        // given
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = getMember("test@test.com", "테스트닉네임");
        Project project = getProject();
        Comment comment = getComment(1L, member, "테스트 댓글", null);
        project.getComments().add(comment);
        CommentRequest request = getCommentRequest(comment.getId(), null);

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        // when
        CommentResponse response = commentService.updateComment(project.getId(), request);

        // then
        assertThat(response.comment().id()).isEqualTo(comment.getId());
        assertThat(response.comment().nickName()).isEqualTo(comment.getMember().getNickName());
        assertThat(project.getComments().size()).isEqualTo(1);
        assertThat(comment.getContent()).isEqualTo(request.content());
    }

    @Test
    @WithMockUser
    public void givenProjectFeedAndComment_whenDeleteComment_thenCommentRemainAndIsRemovedTrue() throws Exception {
        // given
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = getMember("test@test.com", "테스트닉네임");
        Project project = getProject();
        Comment comment = getComment(1L, member, "테스트 댓글", null);
        project.getComments().add(comment);

        CommentDeleteRequest request = getCommentDeleteRequest(comment.getId());
        CommentRequest commentRequest = getCommentRequest(null, null);

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(customCommentRepository.findAllByProject(project.getId())).thenReturn(project.getComments());

        // when
        CommentResponse response = commentService.deleteComment(project.getId(), request);

        CommentListResponse comments = commentService.getAllCommentsByProjectId(project.getId());

        // then
        assertThat(response.message()).isEqualTo("댓글 삭제가 완료됐습니다.");
        assertThat(project.getComments().get(0).isRemoved()).isTrue();
        assertThat(project.getComments().size()).isEqualTo(1);
        assertThat(comments.comments().get(0).content()).isEqualTo("삭제된 댓글입니다.");
        assertThrows(SodevApplicationException.class, () -> commentService.updateComment(project.getId(), commentRequest));
    }

    @Test
    @WithMockUser
    public void givenProjectFeedAndParentChildrenComments_whenDeleteParentComment_thenChildrenCommentsRemain() throws Exception {
        // given
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = getMember("test@test.com", "테스트닉네임");
        Project project = getProject();
        Comment comment1 = getComment(1L, member, "테스트 댓글", null);
        Comment comment2 = getComment(2L, member, "테스트 댓글", comment1);
        project.getComments().add(comment1);
        project.getComments().add(comment2);

        CommentDeleteRequest request = getCommentDeleteRequest(comment1.getId());
        CommentRequest child1Request = getCommentRequest(comment2.getId(), comment1.getId());

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment1));

        // when
        CommentResponse response = commentService.deleteComment(project.getId(), request);
        em.flush();
        em.clear();

        // then
        assertThat(response.message()).isEqualTo("댓글 삭제가 완료됐습니다.");
        assertThat(project.getComments().size()).isEqualTo(2);
        assertThat(project.getComments().get(0).isRemoved()).isTrue();
    }

    @Test
    @WithMockUser
    public void givenProjectFeedAndParentChildrenComments_whenAllCommentsDeleted_thenAllCommentsRemove() throws Exception {
        // given
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = getMember("test@test.com", "테스트닉네임");
        Project project = getProject();
        Comment comment1 = getComment(1L, member, "테스트 댓글", null);
        Comment comment2 = getComment(2L, member, "테스트 댓글", comment1);
        project.getComments().add(comment1);
        project.getComments().add(comment2);

        CommentDeleteRequest commentRequest1 = getCommentDeleteRequest(comment1.getId());
        CommentDeleteRequest commentRequest2 = getCommentDeleteRequest(comment2.getId());

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment1));
        when(commentRepository.findById(2L)).thenReturn(Optional.of(comment2));
        when(customCommentRepository.findAllByProject(project.getId())).thenReturn(project.getComments());


        // when && then
        CommentResponse response1 = commentService.deleteComment(project.getId(), commentRequest1);
        CommentResponse response2 = commentService.deleteComment(project.getId(), commentRequest2);

        assertThat(response1.message()).isEqualTo("댓글 삭제가 완료됐습니다.");
        assertThat(project.getComments().get(0).isRemoved()).isTrue();
        assertThat(response2.message()).isEqualTo("댓글 삭제가 완료됐습니다.");
        assertThat(project.getComments().get(1).isRemoved()).isTrue();
    }

}
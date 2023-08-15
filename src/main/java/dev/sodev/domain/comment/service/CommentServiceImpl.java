package dev.sodev.domain.comment.service;

import dev.sodev.domain.comment.Comment;
import dev.sodev.domain.comment.dto.CommentDto;
import dev.sodev.domain.comment.dto.request.CommentDeleteRequest;
import dev.sodev.domain.comment.dto.request.CommentRequest;
import dev.sodev.domain.comment.dto.response.CommentListResponse;
import dev.sodev.domain.comment.dto.response.CommentResponse;
import dev.sodev.domain.comment.repsitory.CommentRepository;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.member.repository.MemberRepository;
import dev.sodev.domain.project.Project;
import dev.sodev.domain.project.repository.ProjectRepository;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import dev.sodev.global.security.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CommentServiceImpl implements CommentService {

    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final CommentRepository commentRepository;

    @Transactional
    @Override
    public CommentResponse createComment(Long projectId, CommentRequest request) {
        Comment comment = request.of(request);
        comment.confirmWriter(getMemberByEmail());
        comment.confirmProject(getProjectById(projectId));
        hasParentComment(request, comment);
        commentRepository.save(comment);
        CommentDto commentDto = CommentDto.of(comment);

        return CommentResponse.builder()
                .message("댓글 작성이 완료됐습니다.")
                .comment(commentDto)
                .build();
    }

    @Override
    public CommentListResponse getAllCommentsByProjectId(Long projectId) {
        Project project = getProjectById(projectId);
        List<Comment> comments = commentRepository.findAllByProject(projectId);
        List<CommentDto> commentDtos = comments.stream().map(CommentDto::of).toList();

        return CommentListResponse.builder()
                .message("해당 게시글의 댓글 조회가 완료됐습니다.")
                .comments(commentDtos)
                .build();
    }

    @Transactional
    @Override
    public CommentResponse updateComment(Long projectId, CommentRequest request) {
        Comment comment = checkCondition(projectId, request.id());
        comment.updateContent(request.content());

        return CommentResponse.builder()
                .message("댓글 수정이 완료됐습니다.")
                .comment(CommentDto.of(comment))
                .build();
    }


    @Transactional
    @Override
    public CommentResponse deleteComment(Long projectId, CommentDeleteRequest request) {
        Comment comment = checkCondition(projectId, request.id());
        comment.remove();

        List<Comment> removableCommentList = comment.findRemovableList();
        commentRepository.deleteAll(removableCommentList);

        return CommentResponse.builder()
                .message("댓글 삭제가 완료됐습니다.")
                .build();
    }

    @Override
    public CommentListResponse getAllCommentsByMember() {
        Member member = getMemberByEmail();

        List<Comment> allByMemberEmail = commentRepository.findAllByMemberEmail(member.getEmail());
        List<CommentDto> comments = allByMemberEmail.stream().map(CommentDto::of).toList();

        return CommentListResponse.builder()
                .message("회원님이 작성하신 댓글들의 조회를 완료했습니다.")
                .comments(comments)
                .build();
    }

    @Override
    public CommentListResponse getAllCommentsByOtherMember(Long memberId) {
        Member member = getMemberById(memberId);

        List<Comment> allByMemberEmail = commentRepository.findAllByMemberId(member.getId());
        List<CommentDto> comments = allByMemberEmail.stream().map(CommentDto::of).toList();

        return CommentListResponse.builder()
                .message("요청하신 작성자의 댓글들을 조회했습니다.")
                .comments(comments)
                .build();
    }

    // 댓글이 삭제됐는지, 삭제 요청하는 회원이 작성 회원과 같은지 확인
    private Comment checkCondition(Long projectId, Long commentId) {
        Member member = getMemberByEmail();
        Project project = getProjectById(projectId);
        Comment comment = getCommentById(commentId);

        return isRemovedAndWriter(member, comment);
    }

    // id로 회원 조회
    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
    }

    // 이메일로 회원 조회
    private Member getMemberByEmail() {
        return memberRepository.findByEmail(SecurityUtil.getMemberEmail()).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
    }

    // id로 프로젝트 조회
    private Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId).orElseThrow(() -> new SodevApplicationException(ErrorCode.FEED_NOT_FOUND));
    }

    // 부모 댓글 조회
    private Comment getParentComment(CommentRequest request) {
        return getCommentById(request.parentId());
    }

    // id로 댓글 조회
    private Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() -> new SodevApplicationException(ErrorCode.COMMENT_NOT_FOUND));
    }

    private void hasParentComment(CommentRequest request, Comment comment) {
        if (request.parentId() != null) {
            comment.confirmParent(getParentComment(request));
        }
    }

    private static Comment isRemovedAndWriter(Member member, Comment comment) {
        if (comment.isRemoved()) {
            throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "이미 삭제된 댓글입니다");
        } else if (!comment.getMember().getId().equals(member.getId())) {
            throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "댓글 작성자가 일치하지 않습니다.");
        }
        return comment;
    }

}

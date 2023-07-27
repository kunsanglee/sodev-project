package dev.sodev.domain.comment.service;

import dev.sodev.domain.comment.Comment;
import dev.sodev.domain.comment.dto.CommentDto;
import dev.sodev.domain.comment.dto.request.CommentDeleteRequest;
import dev.sodev.domain.comment.dto.request.CommentRequest;
import dev.sodev.domain.comment.dto.response.CommentListResponse;
import dev.sodev.domain.comment.dto.response.CommentResponse;
import dev.sodev.domain.comment.repsitory.CommentRepository;
import dev.sodev.domain.comment.repsitory.CommentCustomRepository;
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
    private final CommentCustomRepository customCommentRepository;

    @Transactional
    @Override
    public CommentResponse createComment(Long projectId, CommentRequest request) {
        Comment comment = request.of(request);
        comment.confirmWriter(memberRepository.findByEmail(SecurityUtil.getMemberEmail()).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND)));
        comment.confirmProject(projectRepository.findById(projectId).orElseThrow(() -> new SodevApplicationException(ErrorCode.FEED_NOT_FOUND)));
        if (request.parentId() != null) {
            comment.confirmParent(commentRepository.findById(request.parentId()).orElseThrow(() -> new SodevApplicationException(ErrorCode.COMMENT_NOT_FOUND)));
        }
        commentRepository.save(comment);

        CommentDto commentDto = CommentDto.of(comment);

        return CommentResponse.builder().message("댓글 작성이 완료됐습니다.").comment(commentDto).build();
    }

    @Override
    public CommentListResponse getAllCommentsByProjectId(Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new SodevApplicationException(ErrorCode.FEED_NOT_FOUND));
        List<Comment> comments = customCommentRepository.findAllByProject(projectId);
        List<CommentDto> commentDtos = comments.stream().map(CommentDto::of).toList();
        return CommentListResponse.builder().message("해당 게시글의 댓글 조회가 완료됐습니다.").comments(commentDtos).build();
    }

    @Transactional
    @Override
    public CommentResponse updateComment(Long projectId, CommentRequest request) {
        Comment comment = checkCondition(projectId, request.id());
        comment.updateContent(request.content());
        return CommentResponse.builder().message("댓글 수정이 완료됐습니다.").comment(CommentDto.of(comment)).build();
    }


    @Transactional
    @Override
    public CommentResponse deleteComment(Long projectId, CommentDeleteRequest request) {
        Comment comment = checkCondition(projectId, request.id());
        comment.remove();

        List<Comment> removableCommentList = comment.findRemovableList();
        commentRepository.deleteAll(removableCommentList);

        return CommentResponse.builder().message("댓글 삭제가 완료됐습니다.").build();
    }

    @Override
    public CommentListResponse getAllCommentsByMember() {
        String memberEmail = SecurityUtil.getMemberEmail();
        Member member = memberRepository.findByEmail(memberEmail).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));

        List<Comment> allByMemberEmail = commentRepository.findAllByMemberEmail(member.getEmail());
        List<CommentDto> comments = allByMemberEmail.stream().map(CommentDto::of).toList();
        return CommentListResponse.builder().message("회원님이 작성하신 댓글들의 조회를 완료했습니다.").comments(comments).build();
    }

    @Override
    public CommentListResponse getAllCommentsByOtherMember(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));

        List<Comment> allByMemberEmail = commentRepository.findAllByMemberId(member.getId());
        List<CommentDto> comments = allByMemberEmail.stream().map(CommentDto::of).toList();
        return CommentListResponse.builder().message("요청하신 작성자의 댓글들을 조회했습니다.").comments(comments).build();
    }

    private Comment checkCondition(Long projectId, Long commentId) {
        Member member = memberRepository.findByEmail(SecurityUtil.getMemberEmail()).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new SodevApplicationException(ErrorCode.FEED_NOT_FOUND));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new SodevApplicationException(ErrorCode.COMMENT_NOT_FOUND));
        if (comment.isRemoved()) {
            throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "이미 삭제된 댓글입니다");
        }
        if (!comment.getMember().getId().equals(member.getId())) { // id로 비교해야 추후 회원이 이메일 변경을 하여도 원래 댓글 작성자와 같은지 비교 가능함.
            throw new SodevApplicationException(ErrorCode.BAD_REQUEST, "댓글 작성자가 일치하지 않습니다.");
        }
        return comment;
    }
}

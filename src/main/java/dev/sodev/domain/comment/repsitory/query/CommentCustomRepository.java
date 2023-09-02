package dev.sodev.domain.comment.repsitory.query;

import dev.sodev.domain.comment.Comment;

import java.util.List;

public interface CommentCustomRepository {

    List<Comment> findAllByProject(Long projectId);
}

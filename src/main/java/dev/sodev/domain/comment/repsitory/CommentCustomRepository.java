package dev.sodev.domain.comment.repsitory;

import dev.sodev.domain.comment.Comment;
import dev.sodev.domain.project.Project;

import java.util.List;

public interface CommentCustomRepository {

    List<Comment> findAllByProject(Project project);
}

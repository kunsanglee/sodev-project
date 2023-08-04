package dev.sodev.domain.comment.repsitory;

import dev.sodev.domain.comment.Comment;
import dev.sodev.domain.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentCustomRepository {

    List<Comment> findAllByMemberEmail(String memberEmail);
    List<Comment> findAllByMemberId(Long id);
}

package dev.sodev.domain.comment.repsitory;

import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.sodev.domain.comment.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static dev.sodev.domain.comment.QComment.*;

@Repository
@RequiredArgsConstructor
public class CommentCustomRepositoryImpl implements CommentCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Comment> findAllByProject(Long projectId) {
        return queryFactory
                .select(comment)
                .from(comment)
                .leftJoin(comment.parent)
                .where(comment.project.id.eq(projectId))
                .orderBy(comment.parent.id.asc().nullsFirst(), comment.createdAt.asc())
                .fetch();
    }
}

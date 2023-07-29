package dev.sodev.domain.likes.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.sodev.domain.likes.Likes;
import dev.sodev.domain.likes.QLikes;
import dev.sodev.domain.likes.dto.LikesMemberDto;
import dev.sodev.domain.likes.dto.LikesProjectDto;
import dev.sodev.domain.project.Project;
import dev.sodev.domain.project.QProject;
import dev.sodev.domain.project.dto.ProjectDto;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static dev.sodev.domain.likes.QLikes.*;
import static dev.sodev.domain.member.QMember.member;
import static dev.sodev.domain.project.QProject.*;


@Repository
public class LikeCustomRepositoryImpl implements LikeCustomRepository{

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;
    private final JdbcTemplate jdbcTemplate;


    public LikeCustomRepositoryImpl(EntityManager em, JdbcTemplate jdbcTemplate) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(JPQLTemplates.DEFAULT,em);
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Likes isProjectLikes(Long id, Long projectId) {
        return queryFactory.selectFrom(likes)
                .where(likes.member.id.eq(id).and(likes.project.id.eq(projectId)))
                .fetchOne();
    }

    @Override
    public List<LikesMemberDto> likeList(Long projectId) {
        return queryFactory.select(Projections.constructor(LikesMemberDto.class, likes.id, member.id, member.nickName))
                .from(likes)
                .join(likes.member, member)
                .where(likes.project.id.eq(projectId))
                .fetch();
    }

    @Override
    public Slice<LikesProjectDto> findLikedProjectsByMemberId(Long memberId, Pageable pageable) {

        List<LikesProjectDto> result = queryFactory
                .select(Projections.constructor(LikesProjectDto.class, likes.id, project.id, project.title))
                .from(likes)
                .where(likes.member.id.eq(memberId))
                .innerJoin(likes.project, project)
                .orderBy(project.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = false;
        if (result.size() == pageable.getPageSize() + 1) {
            hasNext = true;
            result.remove(result.size() - 1);
        }

        return new SliceImpl<>(result, pageable, hasNext);
    }
}

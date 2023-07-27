package dev.sodev.domain.likes.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.sodev.domain.likes.Likes;
import dev.sodev.domain.likes.QLikes;
import dev.sodev.domain.likes.dto.LikesDto;
import dev.sodev.domain.member.QMember;
import jakarta.persistence.EntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static dev.sodev.domain.likes.QLikes.*;
import static dev.sodev.domain.member.QMember.member;


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
    public List<LikesDto> likeList(Long projectId) {
        return queryFactory.select(Projections.constructor(LikesDto.class, likes.id, member.id, member.nickName))
                .from(likes)
                .join(likes.member, member)
                .where(likes.project.id.eq(projectId))
                .fetch();
    }
}

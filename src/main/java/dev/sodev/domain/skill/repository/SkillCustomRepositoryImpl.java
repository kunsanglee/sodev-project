package dev.sodev.domain.skill.repository;

import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;



@Repository
public class SkillCustomRepositoryImpl implements SkillCustomRepository{


    private final EntityManager em;
    private final JPAQueryFactory queryFactory;
    private final JdbcTemplate jdbcTemplate;


    public SkillCustomRepositoryImpl(EntityManager em, JdbcTemplate jdbcTemplate) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(JPQLTemplates.DEFAULT,em);
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    @Transactional
    public void bulkUsageUpdate(List<Integer> skills){
            String sql = "UPDATE skill SET usage = + 1 where skill_id =" +
                    "(?)";

            jdbcTemplate.batchUpdate(sql,
                    skills,
                    skills.size(),
                    (PreparedStatement ps, Integer skill) -> {
                        ps.setInt(1, skill);
                    });

    }


}

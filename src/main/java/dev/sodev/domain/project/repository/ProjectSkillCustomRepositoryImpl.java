package dev.sodev.domain.project.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.sodev.domain.project.dto.projectDTO;
import dev.sodev.domain.project.dto.skillDTO;
import dev.sodev.domain.skill.Skill;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;
import static dev.sodev.domain.project.QProject.project;
import static dev.sodev.domain.project.QProjectSkill.projectSkill;
import static dev.sodev.domain.skill.QSkill.skill;

@Repository
public class ProjectSkillCustomRepositoryImpl implements ProjectSkillCustomRepository{

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;
    private final JdbcTemplate jdbcTemplate;


    public ProjectSkillCustomRepositoryImpl(EntityManager em, JdbcTemplate jdbcTemplate) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(JPQLTemplates.DEFAULT,em);
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<projectDTO> findProject(Long projectId) {
        return queryFactory.selectFrom(projectSkill)
                .leftJoin(projectSkill.project, project)
                .leftJoin(projectSkill.skill,skill)
                .on(project.id.eq(projectId))
                .transform(
                        groupBy(project.id).list(
                            Projections.fields(projectDTO.class, list(Projections.fields(skillDTO.class, skill.name)).as("skills"),
                                    project.fe, project.be,
                                    project.title, project.content,
                                    project.startDate, project.endDate, project.recruitDate,
                                    project.createdAt, project.createdBy,
                                    project.modifiedAt, project.modifiedBy)))
                ;
    }

    @Transactional
    public void saveAll(List<Skill> skills, Long projectId) {
        String sql = "INSERT INTO project_skill (project_id, skill_id) " +
                "VALUES (?, ?)";

        jdbcTemplate.batchUpdate(sql,
                skills,
                skills.size(),
                (PreparedStatement ps, Skill skill) -> {
                    ps.setLong(1, projectId);
                    ps.setLong(2, skill.getId());
                });
    }





}

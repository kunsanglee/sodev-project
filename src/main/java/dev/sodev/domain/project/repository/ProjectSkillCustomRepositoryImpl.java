package dev.sodev.domain.project.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.sodev.domain.enums.ProjectState;
import dev.sodev.domain.project.dto.ProjectDto;
import dev.sodev.domain.project.dto.SkillDto;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

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
    public Optional<ProjectDto> findProject(Long projectId) {
        List<ProjectDto> projectDtoList = queryFactory.selectFrom(projectSkill)
                .leftJoin(projectSkill.project, project)
                .leftJoin(projectSkill.skill, skill)
                .where(project.id.eq(projectId))
                .transform(
                        groupBy(project.id).list(
                                Projections.fields(ProjectDto.class, list(Projections.fields(SkillDto.class, skill.name)).as("skills"),
                                        project.id, project.registeredBy,
                                        project.fe, project.be,
                                        project.title, project.content, project.state,
                                        project.startDate, project.endDate, project.recruitDate,
                                        project.createdAt, project.createdBy,
                                        project.modifiedAt, project.modifiedBy)));

        if (!projectDtoList.isEmpty()) {
            return Optional.of(projectDtoList.get(0));
        } else {
            throw new SodevApplicationException(ErrorCode.BAD_REQUEST);
        }
    }

    @Transactional
    public void saveAll(List<Integer> skills, Long projectId) {
        String sql = "INSERT INTO project_skill (project_id, skill_id) " +
                "VALUES (?, ?)";

        jdbcTemplate.batchUpdate(sql,
                skills,
                skills.size(),
                (PreparedStatement ps, Integer skill) -> {
                    ps.setLong(1, projectId);
                    ps.setLong(2, skill);
                });
    }

    @Override
    public Page<ProjectDto> searchAll(Pageable pageable) {
        List<ProjectDto> content = queryFactory.selectFrom(projectSkill)
                .leftJoin(projectSkill.project, project)
                .leftJoin(projectSkill.skill, skill)
                .where(project.state.eq(ProjectState.RECRUIT))
                .orderBy(project.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .transform(
                        groupBy(project.id).list(
                                Projections.fields(ProjectDto.class, list(Projections.fields(SkillDto.class, skill.name)).as("skills"),
                                        project.id,project.registeredBy,
                                        project.fe, project.be,
                                        project.title, project.content,
                                        project.startDate, project.endDate, project.recruitDate,
                                        project.createdAt, project.createdBy,
                                        project.modifiedAt, project.modifiedBy)))
                ;

        JPAQuery<Long> countQuery = queryFactory
                .select(project.count())
                .from(project)
                .where(project.state.eq(ProjectState.RECRUIT));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<ProjectDto> searchFromEmail(String keyword, List<String> SkillSet, Pageable pageable) {
        List<ProjectDto> content = queryFactory.selectFrom(projectSkill)
                .leftJoin(projectSkill.project, project)
                .leftJoin(projectSkill.skill, skill)
                .where(project.createdBy.eq(keyword).and(skillCheck(SkillSet)).and(project.state.eq(ProjectState.RECRUIT)))
                .orderBy(project.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .transform(
                        groupBy(project.id).list(

                                Projections.fields(ProjectDto.class, list(Projections.fields(SkillDto.class, skill.name)).as("skills"),
                                        project.id,project.registeredBy,
                                        project.fe, project.be,
                                        project.title, project.content,
                                        project.startDate, project.endDate, project.recruitDate,
                                        project.createdAt, project.createdBy,
                                        project.modifiedAt, project.modifiedBy)))
                ;

        JPAQuery<Long> countQuery = queryFactory
                .select(project.count())
                .from(project)
                .where(project.createdBy.eq(keyword).and(project.state.eq(ProjectState.RECRUIT)));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<ProjectDto> searchFromTitle(String keyword, List<String> SkillSet, Pageable pageable) {
        List<ProjectDto> content = queryFactory.selectFrom(projectSkill)
                .leftJoin(projectSkill.project, project)
                .leftJoin(projectSkill.skill, skill)
                .where(project.title.contains(keyword).and(skillCheck(SkillSet)).and(project.state.eq(ProjectState.RECRUIT)))
                .orderBy(project.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .transform(
                        groupBy(project.id).list(
                                Projections.fields(ProjectDto.class, list(Projections.fields(SkillDto.class, skill.name)).as("skills"),
                                        project.id,project.registeredBy,
                                        project.fe, project.be,
                                        project.title, project.content,
                                        project.startDate, project.endDate, project.recruitDate,
                                        project.createdAt, project.createdBy,
                                        project.modifiedAt, project.modifiedBy)))
                ;

        JPAQuery<Long> countQuery = queryFactory
                .select(project.count())
                .from(project)
                .where(project.title.contains(keyword).and(project.state.eq(ProjectState.RECRUIT)));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<ProjectDto> searchFromContent(String keyword, List<String> SkillSet, Pageable pageable) {
        List<ProjectDto> content = queryFactory.selectFrom(projectSkill)
                .leftJoin(projectSkill.project, project)
                .leftJoin(projectSkill.skill, skill)
                .where(project.content.contains(keyword).and(skillCheck(SkillSet)).and(project.state.eq(ProjectState.RECRUIT)))
                .orderBy(project.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .transform(
                        groupBy(project.id).list(
                                Projections.fields(ProjectDto.class, list(Projections.fields(SkillDto.class, skill.name)).as("skills"),
                                        project.id,project.registeredBy,
                                        project.fe, project.be,
                                        project.title, project.content,
                                        project.startDate, project.endDate, project.recruitDate,
                                        project.createdAt, project.createdBy,
                                        project.modifiedAt, project.modifiedBy)))
                ;

        JPAQuery<Long> countQuery = queryFactory
                .select(project.count())
                .from(project)
                .where(project.content.contains(keyword).and(project.state.eq(ProjectState.RECRUIT)));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
    @Override
    public Page<ProjectDto> searchFromNickname(String keyword,List<String> SkillSet, Pageable pageable) {
        List<ProjectDto> content = queryFactory.selectFrom(projectSkill)
                .leftJoin(projectSkill.project, project)
                .leftJoin(projectSkill.skill, skill)
                .where(project.registeredBy.eq(keyword).and(skillCheck(SkillSet)).and(project.state.eq(ProjectState.RECRUIT)))
                .orderBy(project.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .transform(
                        groupBy(project.id).list(
                                Projections.fields(ProjectDto.class, list(Projections.fields(SkillDto.class, skill.name)).as("skills"),
                                        project.id,project.registeredBy,
                                        project.fe, project.be,
                                        project.title, project.content,
                                        project.startDate, project.endDate, project.recruitDate,
                                        project.createdAt, project.createdBy,
                                        project.modifiedAt, project.modifiedBy)))
                ;

        JPAQuery<Long> countQuery = queryFactory
                .select(project.count())
                .from(project)
                .where(project.createdBy.eq(keyword).and(project.state.eq(ProjectState.RECRUIT)));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<ProjectDto> searchFromSkill(List<String> skillset, Pageable pageable) {
        List<ProjectDto> content = queryFactory.selectFrom(projectSkill)
                .leftJoin(projectSkill.project, project)
                .leftJoin(projectSkill.skill, skill)
                .where(skill.name.in(skillset).and(project.state.eq(ProjectState.RECRUIT)))
                .orderBy(project.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .transform(
                        groupBy(project.id).list(
                                Projections.fields(ProjectDto.class, list(Projections.fields(SkillDto.class, skill.name)).as("skills"),
                                        project.id,project.registeredBy,
                                        project.fe, project.be,
                                        project.title, project.content,
                                        project.startDate, project.endDate, project.recruitDate,
                                        project.createdAt, project.createdBy,
                                        project.modifiedAt, project.modifiedBy)))
                ;

        JPAQuery<Long> countQuery = queryFactory
                .select(project.count())
                .from(project)
                .where(skill.name.in(skillset).and(project.state.eq(ProjectState.RECRUIT)));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    public BooleanExpression skillCheck(List<String> skillSet) {
        return skillSet != null ? skill.name.in(skillSet) : null ;
    }



}

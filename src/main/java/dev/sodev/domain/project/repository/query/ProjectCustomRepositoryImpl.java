package dev.sodev.domain.project.repository.query;

import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.sodev.domain.project.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static dev.sodev.domain.project.QProject.project;
@Repository
@RequiredArgsConstructor
public class ProjectCustomRepositoryImpl implements ProjectCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Project> findProject(String nickName, String content) {
        return Optional.ofNullable(
                queryFactory.selectFrom(project)
                            .where(project.createdBy.eq(nickName).and(project.content.eq(content)))
                            .fetchOne());
    }
}

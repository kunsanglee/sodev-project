package dev.sodev.domain.project.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.sodev.domain.project.Project;
import dev.sodev.domain.project.QProject;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
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

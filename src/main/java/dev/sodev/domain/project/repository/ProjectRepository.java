package dev.sodev.domain.project.repository;

import dev.sodev.domain.project.Project;
import dev.sodev.domain.project.repository.query.ProjectCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long>, ProjectCustomRepository {

    @Query("SELECT p FROM Project p JOIN FETCH p.members WHERE p.id = :projectId")
    Optional<Project> findByIdWithMembers(@Param("projectId") Long projectId);

    @Modifying
    @Query("delete from Project p where p.registeredBy = :email")
    void deleteAllByCreatedBy(String email);

    List<Project> findAllByCreatedBy(String email);
}

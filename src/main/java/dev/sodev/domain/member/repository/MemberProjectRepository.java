package dev.sodev.domain.member.repository;

import dev.sodev.domain.member.MemberProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberProjectRepository extends JpaRepository<MemberProject, Long> {

    void deleteAllByProjectId(Long project_id);

    List<MemberProject> findAllByProjectId(Long projectId);
}

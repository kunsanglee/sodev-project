package dev.sodev.domain.member.repository;

import dev.sodev.domain.member.MemberProject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberProjectRepository extends JpaRepository<MemberProject, Long> {
    void deleteAllByProjectId(Long project_id);
}

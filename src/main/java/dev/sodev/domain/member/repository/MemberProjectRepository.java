package dev.sodev.domain.member.repository;

import dev.sodev.domain.member.MemberProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberProjectRepository extends JpaRepository<MemberProject, Long>, MemberProjectCustomRepository {

    List<MemberProject> findAllByProjectId(Long projectId);

    Optional<MemberProject> findAllByMemberId(Long memberId);

    MemberProject getReferenceByMemberId(Long memberId);

    void deleteByProject_IdAndMember_Id(Long projectId, Long memberId);

    @Modifying
    @Query("delete from MemberProject m where m.project.id = :projectId")
    void deleteAllByProjectId(Long projectId);

    @Modifying
    @Query("delete from MemberProject m where m.member.id = :memberId")
    void deleteAllByMemberId(Long memberId);

}

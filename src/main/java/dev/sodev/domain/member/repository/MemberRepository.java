package dev.sodev.domain.member.repository;

import dev.sodev.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberProjectCustomRepository {

    Optional<Member> findByEmail(String email);

    @Query("select m from Member m left join fetch m.comments c where m.email = :email")
    Optional<Member> findByEmailWithComments(String email);

    boolean existsByEmail(String email);

    boolean existsByNickName(String nickName);

    Member getReferenceByEmail(String userId);
}

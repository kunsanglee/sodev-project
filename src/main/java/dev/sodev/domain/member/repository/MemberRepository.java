package dev.sodev.domain.member.repository;

import dev.sodev.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByRefreshToken(String refreshToken);

    boolean existsByEmail(String email);

    boolean existsByNickName(String nickName);
}
